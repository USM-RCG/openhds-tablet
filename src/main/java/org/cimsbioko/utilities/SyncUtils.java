package org.cimsbioko.utilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import org.cimsbioko.activity.SupervisorActivity;
import org.cimsbioko.provider.ContentProvider;
import org.cimsbioko.provider.DatabaseAdapter;
import org.cimsbioko.syncadpt.SyncCancelReceiver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.os.Environment.getExternalStorageDirectory;
import static com.github.batkinson.jrsync.zsync.ZSync.sync;
import static java.net.HttpURLConnection.*;
import static org.cimsbioko.App.AUTHORITY;
import static org.cimsbioko.provider.ContentProvider.DATABASE_NAME;
import static org.cimsbioko.syncadpt.Constants.ACCOUNT_TYPE;
import static org.cimsbioko.utilities.ConfigUtils.getPreferenceBool;
import static org.cimsbioko.utilities.ConfigUtils.getPreferenceString;
import static org.cimsbioko.utilities.HttpUtils.encodeBearerCreds;
import static org.cimsbioko.utilities.HttpUtils.get;
import static org.cimsbioko.utilities.NotificationUtils.*;
import static org.cimsbioko.utilities.StringUtils.join;
import static org.cimsbioko.utilities.UrlUtils.buildServerUrl;

/**
 * Dumping grounds for miscellaneous sync-related functions.
 */
public class SyncUtils {

    private static final String TAG = SyncUtils.class.getSimpleName();

    public static final int SYNC_NOTIFICATION_ID = 42;

    private static final int BUFFER_SIZE = 8192;

    public static final String SQLITE_MIME_TYPE = "application/x-sqlite3";
    public static final String DATA_INSTALLED_ACTION = "DATA_INSTALLED";
    public static final String SYNC_CANCELLED_ACTION = "org.cimsbioko.SYNC_CANCELLED";

    /**
     * Generates a filename to use for storing the ETag header value for a file. The value generated is deterministic
     * and will always generate the same name for a specified file.
     *
     * @param filename the original file name, without preceding path
     * @return filename to store the etag value
     */
    public static String hashFilename(String filename) {
        return String.format("%s.etag", filename);
    }

    /**
     * Generates a filename to use for storing temporary content, which may replace the content of the original file
     * specified. The value is deterministic and will always generate the same name for the specified file.
     *
     * @param filename the original file name, without preceding path
     * @return filename to store intermediate content
     */
    public static String tempFilename(String filename) {
        return String.format("%s.tmp", filename);
    }

    /**
     * Returns the location of the sqlite database file used to store application data.
     *
     * @param ctx application context to use for determining database directory path
     * @return a {@link File} object corresponding to the application's main sqlite database
     */
    public static File getDatabaseFile(Context ctx) {
        return ctx.getDatabasePath(DATABASE_NAME);
    }

    /**
     * Returns the location of the temp file for the specified file.
     *
     * @param original {@link File} object corresponding to location of original file
     * @return {@link File} object corresponding to the temp file location for the original
     */
    public static File getTempFile(File original) {
        return new File(original.getParentFile(), tempFilename(original.getName()));
    }

    /**
     * Returns the location of the fingerprint file for the specified file.
     *
     * @param original {@link File} object corresponding to the location of the original file
     * @return {@link File} object corresponding to the fingerprint file for the original
     */
    public static File getFingerprintFile(File original) {
        return new File(original.getParentFile(), hashFilename(original.getName()));
    }

    /**
     * Returns the {@link URL} to use to fetch sqlite database updates from on the server. It is constructed based on
     * the application's configured server endpoint.
     *
     * @param ctx application context to use for relevant config values
     * @return a {@link URL} object corresponding to the sync endpoint for fetching app sqlite db updates
     * @throws MalformedURLException when the constructed value is not a valid URL
     */
    public static URL getSyncEndpoint(Context ctx) throws MalformedURLException {
        return new URL(buildServerUrl(ctx, ctx.getString(R.string.sync_database_path)));
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
     * A convenience method to safely and easily close multiple closable resources. This is really only necessary until
     * the API level is high enough to use try-with-resources. It swallows any {@link IOException} occurring and logs
     * them at warn.
     *
     * @param closeables a list of possibly null closeable references or null
     */
    public static void close(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable c : closeables) {
                if (c != null) {
                    try {
                        c.close();
                    } catch (IOException e) {
                        Log.w(TAG, "failure during close", e);
                    }
                }
            }
        }
    }

    /**
     * A convenience method to wrap an output stream with a buffer to minimize system calls for multiple writes.
     *
     * @param toWrap the stream to wrap with a buffered stream
     * @return a {@link BufferedOutputStream} wrapping toWrap
     */
    public static OutputStream buffer(OutputStream toWrap) {
        return new BufferedOutputStream(toWrap);
    }

    /**
     * A convenience method to wrap an input stream with a buffer to minimize system calls for multiple reads.
     *
     * @param toWrap the stram to wrap
     * @return a {@link BufferedInputStream} wrapping toWrap
     */
    public static InputStream buffer(InputStream toWrap) {
        return new BufferedInputStream(toWrap);
    }

    /**
     * Returns the first line of the specified file's contents.
     *
     * @param file the file to read
     * @return the first line of content as delimited by system line separator
     */
    private static String loadFirstLine(File file) {
        String line = null;
        if (file.exists() && file.canRead()) {
            try {
                InputStream in = new FileInputStream(file);
                BufferedReader buf = new BufferedReader(new InputStreamReader(in));
                try {
                    line = buf.readLine();
                } finally {
                    close(in);
                }
            } catch (FileNotFoundException e) {
                Log.w(TAG, "file " + file + " not found for reading");
            } catch (IOException e) {
                Log.w(TAG, "reading " + file + "failed", e);
            }
        }
        return line;
    }

    /**
     * Stores the specified string in the specified file, creating if necessary.
     *
     * @param file the location of the file to write to
     * @param s    the string to store
     */
    private static void store(File file, String s) {
        if (!file.exists() || (file.exists() && file.canWrite())) {
            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
                PrintWriter writer = new PrintWriter(out);
                writer.println(s == null ? "" : s);
                writer.flush();
            } catch (FileNotFoundException e) {
                Log.w(TAG, "file: " + file + " not found for storing");
            } finally {
                close(out);
            }
        }
    }

    interface StreamListener {
        void streamed(int bytes);
    }

    /**
     * Convenience method for {@link SyncUtils#streamToFile(InputStream, File, StreamListener)}.
     */
    public static void streamToFile(InputStream in, File f) throws IOException, InterruptedException {
        streamToFile(in, f, null);
    }

    /**
     * Streams the contents of the specified {@link InputStream} to a specified
     * location, always closing the stream.
     *
     * @param in stream to read contents from
     * @param f  location to write contents to
     * @param listener observer to receive notifications of streaming events
     * @throws IOException
     * @throws InterruptedException
     */
    public static void streamToFile(InputStream in, File f, StreamListener listener) throws IOException, InterruptedException {
        OutputStream out = buffer(new FileOutputStream(f));
        try {
            streamToStream(in, out, listener);
        } finally {
            close(out);
        }
    }

    /**
     * Streams the contents of the specified {@link InputStream} to {@link OutputStream}. It does *not* close either
     * streams.
     *
     * @param in the stream to read from
     * @param out the stream to write to
     * @param listener object to use to notify status
     * @throws IOException
     * @throws InterruptedException
     */
    private static void streamToStream(InputStream in, OutputStream out, StreamListener listener)
            throws IOException, InterruptedException {
        byte[] buf = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buf)) >= 0) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            out.write(buf, 0, read);
            if (listener != null) {
                listener.streamed(read);
            }
        }
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
     *
     * @param ctx used to gain access to {@link android.content.ContentProvider} and {@link AccountManager} required to
     *            make sync request.
     */
    public static void checkForUpdate(Context ctx) {
        AccountManager manager = AccountManager.get(ctx);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            Log.i(TAG, "sync requested manually");
            Bundle extras = new Bundle();
            extras.putBoolean(ctx.getString(R.string.manual_sync_key), true);
            ctx.getContentResolver().requestSync(accounts[0], AUTHORITY, extras);
        } else {
            Log.w(TAG, "sync request ignored, no account");
        }
    }

    public static void cancelUpdate(Context ctx) {
        AccountManager manager = AccountManager.get(ctx);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            Log.i(TAG, "sync cancellation requested by user");
            ctx.getContentResolver().cancelSync(accounts[0], AUTHORITY);
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
                            StreamListener progressListener = new StreamListener() {
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
                        Intent intent = new Intent(ctx, SupervisorActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pending = PendingIntent.getActivity(ctx, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        notificationManager.notify(SYNC_NOTIFICATION_ID, builder
                                .setSmallIcon(getNotificationIcon())
                                .setContentText(ctx.getString(R.string.sync_database_new_data_instructions))
                                .setContentIntent(pending)
                                .setProgress(0, 0, false)
                                .setOngoing(false)
                                .build());
                    } catch (IOException | NoSuchAlgorithmException e) {
                        Log.e(TAG, "sync io failure", e);
                        db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "error: " + httpResult);
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
     * Copies the contents of one file to another.
     *
     * @param source {@link File} specifying location of file to copy
     * @param target {@link File} specifying location to copy to
     * @throws IOException
     */
    private static void copyFile(File source, File target) throws IOException {
        FileInputStream sStream = new FileInputStream(source);
        FileOutputStream tStream = new FileOutputStream(target);
        FileChannel sChannel = sStream.getChannel(), tChannel = tStream.getChannel();
        long sourceSize = sChannel.size(), position = 0;
        while (position < sourceSize) {
            position += sChannel.transferTo(position, BUFFER_SIZE, tChannel);
        }
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

            Intent intent = new Intent(ctx, SupervisorActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pending = PendingIntent.getActivity(ctx, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationManager manager = NotificationUtils.getNotificationManager(ctx);
            manager.notify(SYNC_NOTIFICATION_ID, new NotificationCompat.Builder(ctx, SYNC_CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(ctx.getString(R.string.sync_database_new_data))
                    .setContentText(ctx.getString(R.string.sync_database_new_data_instructions))
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

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * Converts the given byte array to its hexadecimal equivalent.
     *
     * @param bytes the array to convert
     * @return the array contents encoded as a base-16 string
     */
    private static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
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
     * Returns the dedicated directory for cims-exclusive data on external storage.
     *
     * @return a {@link File} indicating where cims-specific (public) files are/can be stored
     */
    public static File getExternalDir() {
        return new File(getExternalStorageDirectory(), "cims");
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
