package org.cimsbioko.utilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.github.batkinson.jrsync.Metadata;
import com.github.batkinson.jrsync.zsync.ProgressTracker;
import com.github.batkinson.jrsync.zsync.RangeRequest;
import com.github.batkinson.jrsync.zsync.RangeRequestFactory;
import org.cimsbioko.App;
import org.cimsbioko.R;
import org.cimsbioko.activity.SyncDbActivity;
import org.cimsbioko.provider.ContentProvider;
import org.cimsbioko.provider.DatabaseAdapter;
import org.cimsbioko.syncadpt.SyncCancelReceiver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.github.batkinson.jrsync.zsync.ZSync.sync;
import static java.net.HttpURLConnection.*;
import static org.cimsbioko.App.AUTHORITY;
import static org.cimsbioko.provider.ContentProvider.DATABASE_NAME;
import static org.cimsbioko.syncadpt.Constants.ACCOUNT_TYPE;
import static org.cimsbioko.utilities.ConfigUtils.getPreferenceBool;
import static org.cimsbioko.utilities.ConfigUtils.getPreferenceString;
import static org.cimsbioko.utilities.EncodingUtils.toHex;
import static org.cimsbioko.utilities.FileUtils.*;
import static org.cimsbioko.utilities.HttpUtils.encodeBearerCreds;
import static org.cimsbioko.utilities.HttpUtils.get;
import static org.cimsbioko.utilities.IOUtils.*;
import static org.cimsbioko.utilities.NotificationUtils.*;
import static org.cimsbioko.utilities.SetupUtils.getCampaignId;
import static org.cimsbioko.utilities.StringUtils.join;
import static org.cimsbioko.utilities.UrlUtils.buildServerUrl;

/**
 * Dumping grounds for miscellaneous sync-related functions.
 */
public class SyncUtils {

    private static final String TAG = SyncUtils.class.getSimpleName();

    public static final int SYNC_NOTIFICATION_ID = 42;

    public static final String SQLITE_MIME_TYPE = "application/x-sqlite3";
    public static final String DATA_INSTALLED_ACTION = "DATA_INSTALLED";
    public static final String SYNC_CANCELLED_ACTION = "org.cimsbioko.SYNC_CANCELLED";

    /**
     * Returns the {@link URL} to use to fetch sqlite database updates from on the server. It is constructed based on
     * the application's configured server endpoint and current campaign.
     *
     * @param ctx application context to use for relevant config values
     * @return a {@link URL} object corresponding to the sync endpoint for fetching app sqlite db updates
     * @throws MalformedURLException when the constructed value is not a valid URL
     */
    public static URL getSyncEndpoint(Context ctx) throws MalformedURLException {
        String baseUrl = buildServerUrl(ctx, ctx.getString(R.string.sync_database_path));
        String campaign = getCampaignId();
        return new URL(campaign == null? baseUrl : baseUrl + "/" + campaign);
    }

    /**
     * Gets the configured sync history retention in days.
     *
     * @param ctx application context to use for config values
     * @return the configured sync history retention in days
     */
    public static int getHistoryRetention(Context ctx) {
        return Integer.parseInt(getPreferenceString(ctx, R.string.sync_history_retention_key, "7"));
    }

    /**
     * Returns whether there appears to be complete downloaded content to use for updating the app sqlite database.
     *
     * @param ctx the app context to use for determining content paths
     * @return true if there appears to be temp content to replace app db, otherwise false
     */
    public static boolean canUpdateDatabase(Context ctx) {
        return getFingerprintFile(getTempFile(getDatabaseFile(ctx))).exists();
    }

    /**
     * Interface for a simple status callback when a database update is successfully applied.
     */
    public interface DatabaseInstallationListener {
        void installed();
    }

    /**
     * Returns the current fingerprint value for the currently installed app db. Note that this may not always be a
     * valid file hash of the content file. Bad values aren't problematic, since the HTTP server will treat mismatches
     * as a cache miss and return the content.
     *
     * @param ctx the app context to use for determiningg file paths
     * @return the content of the fingerprint file for the app sqlite database
     */
    public static String getDatabaseFingerprint(Context ctx) {
        String fingerprint = loadFirstLine(getFingerprintFile(getDatabaseFile(ctx)));
        return fingerprint != null ? fingerprint : ctx.getString(R.string.sync_database_no_fingerprint);
    }

    /**
     * Returns whether zsync-based download optimization is currently enabled based on user settings.
     *
     * @param ctx the app context to use for accessing preferences
     * @return true if zsync optimization is enabled, otherwise false
     */
    public static boolean isZyncEnabled(Context ctx) {
        return getPreferenceBool(ctx, ctx.getString(R.string.use_zsync_key), true);
    }

    /**
     * Requests a database synchronization occur for the CIMS account.
     */
    public static void checkForUpdate() {
        Context ctx = App.getApp();
        AccountManager manager = AccountManager.get(ctx);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            Log.i(TAG, "sync requested manually");
            Bundle extras = new Bundle();
            extras.putBoolean(ctx.getString(R.string.manual_sync_key), true);
            ContentResolver.requestSync(accounts[0], AUTHORITY, extras);
        } else {
            Log.w(TAG, "sync request ignored, no account");
        }
    }

    public static void cancelUpdate(Context ctx) {
        AccountManager manager = AccountManager.get(ctx);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            Log.i(TAG, "sync cancellation requested by user");
            ContentResolver.cancelSync(accounts[0], AUTHORITY);
            NotificationUtils.getNotificationManager(ctx).cancel(SYNC_NOTIFICATION_ID);
        } else {
            Log.w(TAG, "sync cancellation ignored, no account");
        }
    }

    public static boolean downloadedContentBefore(Context ctx) {
        return getFingerprintFile(getDatabaseFile(ctx)).exists();
    }

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
    public static void downloadUpdate(final Context ctx, URL endpoint, String accessToken) {

        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(ctx);
        final NotificationManager notificationManager = NotificationUtils.getNotificationManager(ctx);

        File dbFile = getDatabaseFile(ctx), dbDir = dbFile.getParentFile(), dbTempFile = getTempFile(dbFile);

        if (!dbDir.exists() && !dbDir.mkdirs()) {
            Log.w(TAG, "failed to create missing dir " + dbDir + ", sync cancelled");
            return;
        }

        String existingFingerprint = loadFirstLine(getFingerprintFile(dbTempFile.exists() ? dbTempFile : dbFile));

        DatabaseAdapter db = DatabaseAdapter.getInstance();

        String fingerprint = "?";

        boolean downloadedContentBefore = downloadedContentBefore(ctx);
        boolean useZsync = isZyncEnabled(ctx) && downloadedContentBefore;

        String accept;
        if (useZsync) {
            accept = join(", ", SQLITE_MIME_TYPE, Metadata.MIME_TYPE);
        } else {
            accept = SQLITE_MIME_TYPE;
        }

        try {

            long startTime = System.currentTimeMillis();
            String creds = accessToken != null? encodeBearerCreds(accessToken) : null;
            HttpURLConnection httpConn = get(endpoint, accept, creds, existingFingerprint);
            int httpResult = httpConn.getResponseCode();

            Intent cancelBroadcast = new Intent(ctx, SyncCancelReceiver.class);
            cancelBroadcast.setAction(SYNC_CANCELLED_ACTION);
            PendingIntent pendingCancelBroadcast = PendingIntent.getBroadcast(ctx, 0, cancelBroadcast, 0);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, SYNC_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setTicker("")
                    .setColor(getNotificationColor(ctx))
                    .setContentTitle(ctx.getString(R.string.sync_database_new_data))
                    .setContentText(ctx.getString(R.string.sync_database_in_progress))
                    .setProgress(0, 0, true)
                    .setOngoing(true)
                    .addAction(R.drawable.ic_cancel, ctx.getString(R.string.cancel_label), pendingCancelBroadcast);

            switch (httpResult) {
                case HTTP_UNAUTHORIZED:
                    if (accessToken != null) {
                        Log.i(TAG, "received unauthorized, invalidating access token");
                        AccountManager.get(ctx).invalidateAuthToken(ACCOUNT_TYPE, accessToken);
                    }
                case HTTP_NOT_MODIFIED:
                    Log.i(TAG, "no update found");
                    break;
                case HTTP_OK:
                    try {
                        notificationManager.cancel(SYNC_NOTIFICATION_ID);
                        File fingerprintFile = getFingerprintFile(dbTempFile);
                        fingerprint = httpConn.getHeaderField("ETag");
                        Log.i(TAG, "update " + fingerprint + " found, fetching");
                        if (fingerprintFile.exists()) {
                            if (!fingerprintFile.delete()) {
                                Log.w(TAG, "failed to clear old fingerprint, user could install partial content!");
                                ctx.getContentResolver().notifyChange(App.CONTENT_BASE_URI, null, false);
                            }
                        }

                        notificationManager.notify(SYNC_NOTIFICATION_ID, builder.build());

                        InputStream responseBody = httpConn.getInputStream();
                        String responseType = httpConn.getContentType().split(";")[0].toLowerCase();

                        if (useZsync && Metadata.MIME_TYPE.equals(responseType)) {
                            File scratch = new File(dbTempFile.getParentFile(), dbTempFile.getName() + ".syncing");
                            if (scratch.exists()) {
                                if (scratch.length() > dbTempFile.length()) {
                                    Log.i(TAG, "resuming partial content (" + scratch.length() + " bytes)");
                                    if (!scratch.renameTo(dbTempFile)) {
                                        Log.w(TAG, "resume failed, failed to move " + scratch);
                                    }
                                } else {
                                    Log.i(TAG, "ignoring partial content (" + scratch.length() + " bytes):" +
                                            " smaller than basis (" + dbTempFile.length() + " bytes)");
                                }
                            }
                            if (!dbTempFile.exists()) {
                                Log.i(TAG, "no downloaded content, copying existing database");
                                copyFile(dbFile, dbTempFile);
                            }
                            RangeRequestFactory factory = new RangeRequestFactoryImpl(endpoint, SQLITE_MIME_TYPE, creds);
                            Log.i(TAG, "syncing incrementally");
                            incrementalSync(responseBody, dbTempFile, scratch, factory, builder, notificationManager, ctx);
                            if (!scratch.renameTo(dbTempFile)) {
                                Log.e(TAG, "failed to install sync result " + scratch);
                            }
                        } else if (SQLITE_MIME_TYPE.equals(responseType)) {
                            Log.i(TAG, "downloading directly");
                            final int totalSize = httpConn.getContentLength();
                            IOUtils.StreamListener progressListener = new IOUtils.StreamListener() {
                                int streamed;
                                int fileProgress;
                                @Override
                                public void streamed(int bytes) {
                                    streamed += bytes;
                                    int nextValue = totalSize <= 0 ? 100 : (int) ((((double) streamed) / totalSize) * 100);
                                    if (nextValue != fileProgress) {
                                        builder.setProgress(totalSize, nextValue, false);
                                        builder.setContentText(ctx.getString(R.string.sync_downloading));
                                        notificationManager.notify(SYNC_NOTIFICATION_ID, builder.build());
                                    }
                                    fileProgress = nextValue;
                                }
                            };
                            streamToFile(responseBody, dbTempFile, totalSize == -1? null : progressListener);
                        } else {
                            Log.w(TAG, "unexpected content type " + responseType);
                        }

                        store(fingerprintFile, fingerprint);  // install fingerprint after downloaded finishes
                        Log.i(TAG, "database downloaded");
                        db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "success");
                        Intent intent = new Intent(ctx, SyncDbActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pending = PendingIntent.getActivity(ctx, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.mActions.clear();
                        notificationManager.notify(SYNC_NOTIFICATION_ID, builder
                                .setSmallIcon(getNotificationIcon())
                                .setContentIntent(pending)
                                .setContentText(ctx.getString(R.string.sync_database_success))
                                .setProgress(0, 0, false)
                                .setOngoing(false)
                                .build());
                    } catch (IOException | NoSuchAlgorithmException e) {
                        Log.e(TAG, "sync io failure", e);
                        db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "error: " + httpResult);
                        builder.mActions.clear();
                        notificationManager.notify(SYNC_NOTIFICATION_ID, builder
                                .setSmallIcon(getNotificationIcon())
                                .setContentText(ctx.getString(R.string.sync_database_failed))
                                .setProgress(0, 0, false)
                                .setOngoing(false)
                                .build());
                    } catch (InterruptedException e) {
                        Log.e(TAG, "sync thread canceled", e);
                        db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "canceled");
                        notificationManager.cancel(SYNC_NOTIFICATION_ID);
                    }
                    break;
                default:
                    Log.i(TAG, "unexpected status code " + httpResult);
            }
        } catch (IOException e) {
            Log.e(TAG, "sync failed: " + e.getMessage());
        }

        db.pruneSyncResults(getHistoryRetention(ctx));

        // automatically install database if it's the first time
        if (!downloadedContentBefore) {
            installUpdate(ctx, () -> {
                broadcastManager.sendBroadcast(new Intent(DATA_INSTALLED_ACTION));
                notificationManager.cancel(SYNC_NOTIFICATION_ID);
            });
        }

        ctx.getContentResolver().notifyChange(App.CONTENT_BASE_URI, null, false);
    }

    /**
     * Reads metadata from the specified stream and closes stream.
     */
    private static Metadata readMetadata(InputStream in) throws IOException, NoSuchAlgorithmException {
        DataInputStream metaIn = new DataInputStream(buffer(in));
        try {
            return Metadata.read(metaIn);
        } finally {
            close(metaIn);
        }
    }

    private static String getStageLabel(Context ctx, ProgressTracker.Stage stage) {
        switch (stage) {
            case SEARCH:
                return ctx.getString(R.string.sync_stage_searching);
            case BUILD:
                return ctx.getString(R.string.sync_stage_building);
            default:
                return "?";
        }
    }

    /**
     * Performs an incremental sync based on a local existing file.
     */
    private static void incrementalSync(InputStream responseBody, File basis, File target, RangeRequestFactory factory,
                                        final NotificationCompat.Builder builder, final NotificationManager manager,
                                        final Context ctx)
            throws NoSuchAlgorithmException, IOException, InterruptedException {
        Metadata metadata = readMetadata(responseBody);
        ProgressTracker tracker = new ProgressTracker() {
            String text = "";
            int percent = -1;
            @Override
            public void onProgress(Stage stage, int percentComplete) {
                if (!text.equals(stage.name()) || percent != percentComplete) {
                    text = stage.name();
                    percent = percentComplete;
                    builder.setContentText(getStageLabel(ctx, stage));
                    builder.setProgress(100, percentComplete, false);
                    manager.notify(SYNC_NOTIFICATION_ID, builder.build());
                }
            }
        };
        sync(metadata, basis, target, factory, tracker);
    }

    /**
     * Replaces the application's sqlite database with previously downloaded content if present and reloads the content
     * provider to make the updated content immediately available to the application.
     *
     * @param ctx      the app context to use for accessing relevant resources
     * @param listener object to use for callback upon successful update
     */
    public static void installUpdate(Context ctx, DatabaseInstallationListener listener) {
        File dbFile = getDatabaseFile(ctx), dbTempFile = getTempFile(dbFile),
                dbFpFile = getFingerprintFile(dbFile), dbTempFpFile = getFingerprintFile(dbTempFile);
        if (canUpdateDatabase(ctx)) {
            if (dbTempFile.renameTo(dbFile) && dbTempFpFile.renameTo(dbFpFile)) {
                ContentProvider.getDatabaseHelper(ctx).close();
                listener.installed();
            } else {
                Log.e(TAG, "failed to install update");
            }
        }
    }

    /**
     * Detects a database file on external storage and makes it available for installation.
     *
     * @param ctx the android {@link Context} to use for label and service lookups
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     */
    public static void makeOfflineDbAvailable(Context ctx)
            throws IOException, NoSuchAlgorithmException, InterruptedException {
        if (offlineDbExists()) {
            File offlineFile = getOfflineDbFile(), installableFile = getTempFile(getDatabaseFile(ctx));
            copyWithFingerprint(ctx.getCacheDir(), offlineFile, installableFile);
            if (!offlineFile.delete()) {
                Log.w(TAG, "failed to remove offline db file after copy");
            }

            ctx.getContentResolver().notifyChange(App.CONTENT_BASE_URI, null, false);

            Intent intent = new Intent(ctx, SyncDbActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pending = PendingIntent.getActivity(ctx, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationManager manager = NotificationUtils.getNotificationManager(ctx);
            manager.notify(SYNC_NOTIFICATION_ID, new NotificationCompat.Builder(ctx, SYNC_CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(ctx.getString(R.string.sync_database_new_data))
                    .setProgress(0, 0, false)
                    .setContentIntent(pending)
                    .setOngoing(false)
                    .build());
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
    private static void copyWithFingerprint(File tmpDir, File src, File dst)
            throws IOException, NoSuchAlgorithmException, InterruptedException {
        File movableFile = File.createTempFile(dst.getName(), null, tmpDir);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        DigestOutputStream out = null;
        FileInputStream in = null;
        try {
            out = new DigestOutputStream(new FileOutputStream(movableFile), digest);
            in = new FileInputStream(src);
            streamToStream(in, out, null);
            if (!movableFile.renameTo(dst)) {
                Log.e(TAG, "failed to move temp file " + movableFile + " to " + dst);
            }
        } finally {
            close(in, out);
        }
        store(getFingerprintFile(dst), toHex(digest.digest()));
    }

    /**
     * Determines whether an offline database file exists on external storage (user put it there).
     *
     * @return true if an offline db file exists in its well-known location, otherwise false
     */
    private static boolean offlineDbExists() {
        return getOfflineDbFile().isFile();
    }

    /**
     * Returns the well-known location for the offline database file.
     *
     * @return a {@link File} object indicating the external storage location referenced for offline db files
     */
    private static File getOfflineDbFile() {
        return new File(getExternalDir(), DATABASE_NAME);
    }

    /**
     * Used to create range requests for fetching remote file data.
     */
    private static class RangeRequestFactoryImpl implements RangeRequestFactory {

        private final URL endpoint;
        private final String mimeType;
        private final String auth;

        public RangeRequestFactoryImpl(URL endpoint, String mimeType, String auth) {
            this.endpoint = endpoint;
            this.mimeType = mimeType;
            this.auth = auth;
        }

        @Override
        public RangeRequest create() throws IOException {
            return new RangeRequestImpl(HttpUtils.get(endpoint, mimeType, auth, null));
        }
    }

    /**
     * A wrapper around android's {@link HttpURLConnection} to make them usable
     * with sync range requests.
     */
    private static class RangeRequestImpl implements RangeRequest {

        private final HttpURLConnection c;

        RangeRequestImpl(HttpURLConnection c) {
            this.c = c;
        }

        @Override
        public int getResponseCode() throws IOException {
            return c.getResponseCode();
        }

        @Override
        public String getContentType() {
            return c.getContentType();
        }

        @Override
        public String getHeader(String name) {
            return c.getHeaderField(name);
        }

        @Override
        public void setHeader(String name, String value) {
            c.setRequestProperty(name, value);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return c.getInputStream();
        }

        @Override
        public void close() {
            c.disconnect();
        }
    }
}
