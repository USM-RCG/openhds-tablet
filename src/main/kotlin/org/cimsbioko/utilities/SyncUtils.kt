package org.cimsbioko.utilities

import android.accounts.AccountManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.batkinson.jrsync.Metadata
import com.github.batkinson.jrsync.zsync.ProgressTracker
import com.github.batkinson.jrsync.zsync.RangeRequest
import com.github.batkinson.jrsync.zsync.RangeRequestFactory
import com.github.batkinson.jrsync.zsync.ZSync
import org.cimsbioko.App
import org.cimsbioko.R
import org.cimsbioko.activity.SyncDbActivity
import org.cimsbioko.provider.ContentProvider
import org.cimsbioko.provider.DatabaseAdapter
import org.cimsbioko.syncadpt.Constants
import org.cimsbioko.syncadpt.SyncCancelReceiver
import org.cimsbioko.utilities.ConfigUtils.getPreferenceBool
import org.cimsbioko.utilities.ConfigUtils.getPreferenceString
import org.cimsbioko.utilities.EncodingUtils.toHex
import org.cimsbioko.utilities.FileUtils.getDatabaseFile
import org.cimsbioko.utilities.FileUtils.getFingerprintFile
import org.cimsbioko.utilities.FileUtils.getTempFile
import org.cimsbioko.utilities.HttpUtils.encodeBearerCreds
import org.cimsbioko.utilities.HttpUtils.get
import org.cimsbioko.utilities.IOUtils.StreamListener
import org.cimsbioko.utilities.IOUtils.buffer
import org.cimsbioko.utilities.IOUtils.close
import org.cimsbioko.utilities.IOUtils.copyFile
import org.cimsbioko.utilities.IOUtils.externalDir
import org.cimsbioko.utilities.IOUtils.loadFirstLine
import org.cimsbioko.utilities.IOUtils.store
import org.cimsbioko.utilities.IOUtils.streamToFile
import org.cimsbioko.utilities.IOUtils.streamToStream
import org.cimsbioko.utilities.NotificationUtils.PROGRESS_NOTIFICATION_RATE_MILLIS
import org.cimsbioko.utilities.NotificationUtils.SYNC_CHANNEL_ID
import org.cimsbioko.utilities.NotificationUtils.getNotificationColor
import org.cimsbioko.utilities.NotificationUtils.getNotificationManager
import org.cimsbioko.utilities.NotificationUtils.notificationIcon
import org.cimsbioko.utilities.SetupUtils.campaignId
import org.cimsbioko.utilities.StringUtils.join
import org.cimsbioko.utilities.UrlUtils.buildServerUrl
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Dumping grounds for miscellaneous sync-related functions.
 */
object SyncUtils {

    private val TAG = SyncUtils::class.java.simpleName

    const val SYNC_NOTIFICATION_ID = 42
    const val DATA_INSTALLED_ACTION = "DATA_INSTALLED"
    private const val SQLITE_MIME_TYPE = "application/x-sqlite3"
    private const val SYNC_CANCELLED_ACTION = "org.cimsbioko.SYNC_CANCELLED"

    /**
     * Returns the [URL] to use to fetch sqlite database updates from on the server. It is constructed based on
     * the application's configured server endpoint and current campaign.
     *
     * @param ctx application context to use for relevant config values
     * @return a [URL] to use for fetching app sqlite db update for the current campaign
     * @throws MalformedURLException when the constructed value is not a valid URL
     */
    @Throws(MalformedURLException::class)
    fun getRemoteSyncEndpoint(ctx: Context): URL {
        val baseUrl = buildServerUrl(ctx, ctx.getString(R.string.sync_database_path))
        val campaign = campaignId
        return URL(if (campaign == null) baseUrl else "$baseUrl/$campaign")
    }

    /**
     * Returns the [URL] to use to fetch sqlite database updates from a sidecar device on the local network. It
     * is constructed based on the resolved network service discovery object and the current campaign.
     *
     * @param ctx application context to use for base url resource lookup
     * @param info the resolved network service discovery information for a sidecar service on local network
     * @return a [URL] to use for fetching app sqlite db update for the current campaign
     * @throws MalformedURLException
     */
    @Throws(MalformedURLException::class)
    fun getLocalSyncEndpoint(ctx: Context, info: NsdServiceInfo): URL =
            URL("http", info.host.hostName, info.port, ctx.getString(R.string.sync_database_path) + "/" + campaignId)

    /**
     * Gets the configured sync history retention in days.
     *
     * @param ctx application context to use for config values
     * @return the configured sync history retention in days
     */
    private fun getHistoryRetention(ctx: Context): Int =
            getPreferenceString(ctx, R.string.sync_history_retention_key, "7")!!.toInt()

    /**
     * Returns whether there appears to be complete downloaded content to use for updating the app sqlite database.
     *
     * @param ctx the app context to use for determining content paths
     * @return true if there appears to be temp content to replace app db, otherwise false
     */
    fun canUpdateDatabase(ctx: Context): Boolean = getFingerprintFile(getTempFile(getDatabaseFile(ctx))).exists()

    /**
     * Returns the current fingerprint value for the currently installed app db. Note that this may not always be a
     * valid file hash of the content file. Bad values aren't problematic, since the HTTP server will treat mismatches
     * as a cache miss and return the content.
     *
     * @param ctx the app context to use for determiningg file paths
     * @return the content of the fingerprint file for the app sqlite database
     */
    fun getDatabaseFingerprint(ctx: Context): String {
        return loadFirstLine(getFingerprintFile(getDatabaseFile(ctx)))
                ?: ctx.getString(R.string.sync_database_no_fingerprint)
    }

    /**
     * Returns whether zsync-based download optimization is currently enabled based on user settings.
     *
     * @param ctx the app context to use for accessing preferences
     * @return true if zsync optimization is enabled, otherwise false
     */
    private fun isZyncEnabled(ctx: Context): Boolean = getPreferenceBool(ctx, ctx.getString(R.string.use_zsync_key), true)

    /**
     * Requests a database synchronization occur for the CIMS account.
     */
    fun checkForUpdate() {
        val ctx: Context = App.instance
        val manager = AccountManager.get(ctx)
        val accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE)
        if (accounts.isNotEmpty()) {
            Log.i(TAG, "sync requested manually")
            val extras = Bundle()
            extras.putBoolean(ctx.getString(R.string.manual_sync_key), true)
            ContentResolver.requestSync(accounts[0], App.AUTHORITY, extras)
        } else {
            Log.w(TAG, "sync request ignored, no account")
        }
    }

    fun cancelUpdate(ctx: Context?) {
        val manager = AccountManager.get(ctx)
        val accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE)
        if (accounts.isNotEmpty()) {
            Log.i(TAG, "sync cancellation requested by user")
            ContentResolver.cancelSync(accounts[0], App.AUTHORITY)
            getNotificationManager(ctx!!).cancel(SYNC_NOTIFICATION_ID)
        } else {
            Log.w(TAG, "sync cancellation ignored, no account")
        }
    }

    fun downloadedContentBefore(ctx: Context): Boolean = getFingerprintFile(getDatabaseFile(ctx)).exists()

    /**
     * Downloads an SQLite database and notifies the user to apply it manually via system notifications. There are two
     * modes of downloading the SQLite file, incremental and direct. By default, incremental downloading is enabled,
     * though the user can disable it via preferences. The first time a database is downloaded, it is downloaded
     * directly. Subsequent downloads use zsync to download only differences from the local content.
     *
     * @param ctx      app context to use for locating resources like files, etc.
     * @param endpoint url of the remote http endpoint to sync with
     * @param accessToken bearer token to use when contacting server
     */
    fun downloadUpdate(ctx: Context, endpoint: URL, accessToken: String?) {
        val broadcastManager = LocalBroadcastManager.getInstance(ctx)
        val notificationManager = getNotificationManager(ctx)
        val dbFile = getDatabaseFile(ctx)
        val dbDir = dbFile.parentFile
        val dbTempFile = getTempFile(dbFile)
        if (!dbDir.exists() && !dbDir.mkdirs()) {
            Log.w(TAG, "failed to create missing dir $dbDir, sync cancelled")
            return
        }
        val existingFingerprint = loadFirstLine(getFingerprintFile(if (dbTempFile.exists()) dbTempFile else dbFile))
        Log.d(TAG, "attempting to update content, fingerprint: $existingFingerprint")
        val db = DatabaseAdapter
        var fingerprint = "?"
        val downloadedContentBefore = downloadedContentBefore(ctx)
        val useZsync = isZyncEnabled(ctx) && downloadedContentBefore
        val accept: String
        accept = if (useZsync) {
            join(", ", SQLITE_MIME_TYPE, Metadata.MIME_TYPE)
        } else {
            SQLITE_MIME_TYPE
        }
        try {
            val startTime = System.currentTimeMillis()
            val creds = accessToken?.let { encodeBearerCreds(it) }
            val httpConn = get(endpoint, accept, creds, existingFingerprint)
            val httpResult = httpConn.responseCode
            val cancelBroadcast = Intent(ctx, SyncCancelReceiver::class.java)
            cancelBroadcast.action = SYNC_CANCELLED_ACTION
            val pendingCancelBroadcast = PendingIntent.getBroadcast(ctx, 0, cancelBroadcast, 0)
            val builder = NotificationCompat.Builder(ctx, SYNC_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setTicker("")
                    .setColor(getNotificationColor(ctx))
                    .setContentTitle(ctx.getString(R.string.sync_database_new_data))
                    .setContentText(ctx.getString(R.string.sync_database_in_progress))
                    .setProgress(0, 0, true)
                    .setOngoing(true)
                    .addAction(R.drawable.ic_cancel, ctx.getString(R.string.cancel_label), pendingCancelBroadcast)
            when (httpResult) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    if (accessToken != null) {
                        Log.i(TAG, "received unauthorized, invalidating access token")
                        AccountManager.get(ctx).invalidateAuthToken(Constants.ACCOUNT_TYPE, accessToken)
                    }
                    Log.i(TAG, "no update found")
                }
                HttpURLConnection.HTTP_NOT_MODIFIED -> Log.i(TAG, "no update found")
                HttpURLConnection.HTTP_OK -> try {
                    notificationManager.cancel(SYNC_NOTIFICATION_ID)
                    val fingerprintFile = getFingerprintFile(dbTempFile)
                    fingerprint = getEtag(httpConn)
                    Log.i(TAG, "update $fingerprint found, fetching")
                    if (fingerprintFile.exists()) {
                        if (!fingerprintFile.delete()) {
                            Log.w(TAG, "failed to clear old fingerprint, user could install partial content!")
                            ctx.contentResolver.notifyChange(App.CONTENT_BASE_URI, null, false)
                        }
                    }
                    notificationManager.notify(SYNC_NOTIFICATION_ID, builder.build())
                    val responseBody = httpConn.inputStream
                    val responseType = httpConn.contentType.split(";".toRegex()).toTypedArray()[0].toLowerCase()
                    if (useZsync && Metadata.MIME_TYPE == responseType) {
                        val scratch = File(dbTempFile.parentFile, dbTempFile.name + ".syncing")
                        if (scratch.exists()) {
                            if (scratch.length() > dbTempFile.length()) {
                                Log.i(TAG, "resuming partial content (" + scratch.length() + " bytes)")
                                if (!scratch.renameTo(dbTempFile)) {
                                    Log.w(TAG, "resume failed, failed to move $scratch")
                                }
                            } else {
                                Log.i(TAG, "ignoring partial content (" + scratch.length() + " bytes):" +
                                        " smaller than basis (" + dbTempFile.length() + " bytes)")
                            }
                        }
                        if (!dbTempFile.exists()) {
                            Log.i(TAG, "no downloaded content, copying existing database")
                            copyFile(dbFile, dbTempFile)
                        }
                        val factory: RangeRequestFactory = RangeRequestFactoryImpl(endpoint, SQLITE_MIME_TYPE, creds)
                        Log.i(TAG, "syncing incrementally")
                        incrementalSync(responseBody, dbTempFile, scratch, factory, builder, notificationManager, ctx)
                        if (!scratch.renameTo(dbTempFile)) {
                            Log.e(TAG, "failed to install sync result $scratch")
                        }
                    } else if (SQLITE_MIME_TYPE == responseType) {
                        Log.i(TAG, "downloading directly")
                        val totalSize = httpConn.contentLength
                        val progressListener: StreamListener = object : StreamListener {
                            var lastUpdate: Long = 0
                            var streamed = 0
                            var fileProgress = 0
                            override fun streamed(bytes: Int) {
                                val thisUpdate = System.currentTimeMillis()
                                if (thisUpdate - lastUpdate > PROGRESS_NOTIFICATION_RATE_MILLIS) {
                                    streamed += bytes
                                    val nextValue = if (totalSize <= 0) 100 else (streamed.toDouble() / totalSize * 100).toInt()
                                    if (nextValue != fileProgress) {
                                        builder.setProgress(totalSize, nextValue, false)
                                        builder.setContentText(ctx.getString(R.string.sync_downloading))
                                        notificationManager.notify(SYNC_NOTIFICATION_ID, builder.build())
                                        lastUpdate = thisUpdate
                                    }
                                    fileProgress = nextValue
                                }
                            }
                        }
                        streamToFile(responseBody, dbTempFile, if (totalSize == -1) null else progressListener)
                    } else {
                        Log.w(TAG, "unexpected content type $responseType")
                    }
                    store(fingerprintFile, fingerprint) // install fingerprint after downloaded finishes
                    Log.i(TAG, "database downloaded")
                    db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "success")
                    val intent = Intent(ctx, SyncDbActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    val pending = PendingIntent.getActivity(ctx, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    builder.mActions.clear()
                    notificationManager.notify(SYNC_NOTIFICATION_ID, builder
                            .setSmallIcon(notificationIcon)
                            .setContentIntent(pending)
                            .setContentText(ctx.getString(R.string.sync_database_success))
                            .setProgress(0, 0, false)
                            .setOngoing(false)
                            .build())
                } catch (e: IOException) {
                    Log.e(TAG, "sync io failure", e)
                    db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "error: $httpResult")
                    builder.mActions.clear()
                    notificationManager.notify(SYNC_NOTIFICATION_ID, builder
                            .setSmallIcon(notificationIcon)
                            .setContentText(ctx.getString(R.string.sync_database_failed))
                            .setProgress(0, 0, false)
                            .setOngoing(false)
                            .build())
                } catch (e: NoSuchAlgorithmException) {
                    Log.e(TAG, "sync io failure", e)
                    db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "error: $httpResult")
                    builder.mActions.clear()
                    notificationManager.notify(SYNC_NOTIFICATION_ID, builder
                            .setSmallIcon(notificationIcon)
                            .setContentText(ctx.getString(R.string.sync_database_failed))
                            .setProgress(0, 0, false)
                            .setOngoing(false)
                            .build())
                } catch (e: InterruptedException) {
                    Log.e(TAG, "sync thread canceled", e)
                    db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "canceled")
                    notificationManager.cancel(SYNC_NOTIFICATION_ID)
                }
                else -> Log.i(TAG, "unexpected status code $httpResult")
            }
        } catch (e: IOException) {
            Log.e(TAG, "sync failed: " + e.message)
        }
        db.pruneSyncResults(getHistoryRetention(ctx))

        // automatically install database if it's the first time
        if (!downloadedContentBefore) {
            installUpdate(ctx, object : DatabaseInstallationListener {
                override fun installed() {
                    broadcastManager.sendBroadcast(Intent(DATA_INSTALLED_ACTION))
                    notificationManager.cancel(SYNC_NOTIFICATION_ID)
                }
            })
        }
        ctx.contentResolver.notifyChange(App.CONTENT_BASE_URI, null, false)
    }

    /**
     * Extracts the etag header in a way that works with AWS ELBs, which lowercase all headers to conform with HTTP/2.
     *
     * @param httpConn the connection to extract the header from.
     * @return the etag header value or null, if none found
     */
    private fun getEtag(httpConn: HttpURLConnection): String {
        val value = httpConn.getHeaderField("etag")
        return value ?: httpConn.getHeaderField("ETag")
    }

    /**
     * Reads metadata from the specified stream and closes stream.
     */
    @Throws(IOException::class, NoSuchAlgorithmException::class)
    private fun readMetadata(`in`: InputStream): Metadata {
        val metaIn = DataInputStream(buffer(`in`))
        return try {
            Metadata.read(metaIn)
        } finally {
            close(metaIn)
        }
    }

    private fun getStageLabel(ctx: Context, stage: ProgressTracker.Stage): String {
        return when (stage) {
            ProgressTracker.Stage.SEARCH -> ctx.getString(R.string.sync_stage_searching)
            ProgressTracker.Stage.BUILD -> ctx.getString(R.string.sync_stage_building)
            else -> "?"
        }
    }

    /**
     * Performs an incremental sync based on a local existing file.
     */
    @Throws(NoSuchAlgorithmException::class, IOException::class, InterruptedException::class)
    private fun incrementalSync(responseBody: InputStream, basis: File, target: File, factory: RangeRequestFactory,
                                builder: NotificationCompat.Builder, manager: NotificationManager,
                                ctx: Context) {
        val metadata = readMetadata(responseBody)
        val tracker: ProgressTracker = object : ProgressTracker {
            var lastUpdate: Long = 0
            var text = ""
            var percent = -1
            override fun onProgress(stage: ProgressTracker.Stage, percentComplete: Int) {
                val thisUpdate = System.currentTimeMillis()
                if (thisUpdate - lastUpdate > PROGRESS_NOTIFICATION_RATE_MILLIS) {
                    if (text != stage.name || percent != percentComplete) {
                        text = stage.name
                        percent = percentComplete
                        builder.setContentText(getStageLabel(ctx, stage))
                        builder.setProgress(100, percentComplete, false)
                        manager.notify(SYNC_NOTIFICATION_ID, builder.build())
                        lastUpdate = thisUpdate
                    }
                }
            }
        }
        ZSync.sync(metadata, basis, target, factory, tracker)
    }

    /**
     * Replaces the application's sqlite database with previously downloaded content if present and reloads the content
     * provider to make the updated content immediately available to the application.
     *
     * @param ctx      the app context to use for accessing relevant resources
     * @param listener object to use for callback upon successful update
     */
    fun installUpdate(ctx: Context, listener: DatabaseInstallationListener) {
        val dbFile = getDatabaseFile(ctx)
        val dbTempFile = getTempFile(dbFile)
        val dbFpFile = getFingerprintFile(dbFile)
        val dbTempFpFile = getFingerprintFile(dbTempFile)
        if (canUpdateDatabase(ctx)) {
            if (dbTempFile.renameTo(dbFile) && dbTempFpFile.renameTo(dbFpFile)) {
                ContentProvider.databaseHelper.close()
                listener.installed()
            } else {
                Log.e(TAG, "failed to install update")
            }
        }
    }

    /**
     * Detects a database file on external storage and makes it available for installation.
     *
     * @param ctx the android [Context] to use for label and service lookups
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     */
    @Throws(IOException::class, NoSuchAlgorithmException::class, InterruptedException::class)
    fun makeOfflineDbAvailable(ctx: Context) {
        if (offlineDbExists()) {
            val offlineFile = offlineDbFile
            val installableFile = getTempFile(getDatabaseFile(ctx))
            copyWithFingerprint(ctx.cacheDir, offlineFile, installableFile)
            if (!offlineFile.delete()) {
                Log.w(TAG, "failed to remove offline db file after copy")
            }
            ctx.contentResolver.notifyChange(App.CONTENT_BASE_URI, null, false)
            val intent = Intent(ctx, SyncDbActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pending = PendingIntent.getActivity(ctx, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val manager = getNotificationManager(ctx)
            manager.notify(SYNC_NOTIFICATION_ID, NotificationCompat.Builder(ctx, SYNC_CHANNEL_ID)
                    .setSmallIcon(notificationIcon)
                    .setContentTitle(ctx.getString(R.string.sync_database_new_data))
                    .setProgress(0, 0, false)
                    .setContentIntent(pending)
                    .setOngoing(false)
                    .build())
        }
    }

    /**
     * Copies the source file to the destination file along with an accompanying fingerprint (stored along side it). It
     * copies to a scratch file and moves the final result to reduce the possibility of strange filesystem semantics due
     * to concurrent copy attempts.
     *
     * @param tmpDir the temp directory to use for an intermediary temp file
     * @param src the file to copy
     * @param dst the destination to copy to
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     */
    @Throws(IOException::class, NoSuchAlgorithmException::class, InterruptedException::class)
    private fun copyWithFingerprint(tmpDir: File, src: File, dst: File) {
        val movableFile = File.createTempFile(dst.name, null, tmpDir)
        val digest = MessageDigest.getInstance("MD5")
        var out: DigestOutputStream? = null
        var `in`: FileInputStream? = null
        try {
            out = DigestOutputStream(FileOutputStream(movableFile), digest)
            `in` = FileInputStream(src)
            streamToStream(`in`, out, null)
            if (!movableFile.renameTo(dst)) {
                Log.e(TAG, "failed to move temp file $movableFile to $dst")
            }
        } finally {
            close(`in`, out)
        }
        store(getFingerprintFile(dst), toHex(digest.digest()))
    }

    /**
     * Determines whether an offline database file exists on external storage (user put it there).
     *
     * @return true if an offline db file exists in its well-known location, otherwise false
     */
    private fun offlineDbExists(): Boolean = offlineDbFile.isFile

    /**
     * Returns the well-known location for the offline database file.
     *
     * @return a [File] object indicating the external storage location referenced for offline db files
     */
    private val offlineDbFile: File
        get() = File(externalDir, ContentProvider.DATABASE_NAME)

    /**
     * Interface for a simple status callback when a database update is successfully applied.
     */
    interface DatabaseInstallationListener {
        fun installed()
    }

    /**
     * Used to create range requests for fetching remote file data.
     */
    private class RangeRequestFactoryImpl(private val endpoint: URL, private val mimeType: String, private val auth: String?) : RangeRequestFactory {
        @Throws(IOException::class)
        override fun create(): RangeRequest {
            return RangeRequestImpl(get(endpoint, mimeType, auth, null))
        }
    }

    /**
     * A wrapper around android's [HttpURLConnection] to make them usable
     * with sync range requests.
     */
    private class RangeRequestImpl internal constructor(private val c: HttpURLConnection) : RangeRequest {

        @Throws(IOException::class)
        override fun getResponseCode(): Int {
            return c.responseCode
        }

        override fun getContentType(): String {
            return c.contentType
        }

        override fun getHeader(name: String): String {
            return c.getHeaderField(name)
        }

        override fun setHeader(name: String, value: String) {
            c.setRequestProperty(name, value)
        }

        @Throws(IOException::class)
        override fun getInputStream(): InputStream {
            return c.inputStream
        }

        override fun close() {
            c.disconnect()
        }

    }
}