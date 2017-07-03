package org.openhds.mobile.utilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.batkinson.jrsync.Metadata;
import com.github.batkinson.jrsync.zsync.ProgressTracker;
import com.github.batkinson.jrsync.zsync.RangeRequest;
import com.github.batkinson.jrsync.zsync.RangeRequestFactory;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.activity.SupervisorActivity;
import org.openhds.mobile.provider.DatabaseAdapter;
import org.openhds.mobile.provider.OpenHDSProvider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;

import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.batkinson.jrsync.zsync.ZSync.sync;
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openhds.mobile.OpenHDS.AUTHORITY;
import static org.openhds.mobile.provider.OpenHDSProvider.DATABASE_NAME;
import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.HttpUtils.encodeBasicCreds;
import static org.openhds.mobile.utilities.HttpUtils.get;
import static org.openhds.mobile.utilities.StringUtils.join;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

/**
 * Dumping grounds for miscellaneous sync-related functions.
 */
public class SyncUtils {

    private static final String TAG = SyncUtils.class.getSimpleName();

    public static final int SYNC_NOTIFICATION_ID = 42;

    private static final int BUFFER_SIZE = 8192;

    public static final String SQLITE_MIME_TYPE = "application/x-sqlite3";

    public static final String ACCOUNT_TYPE = "cims-bioko.org";

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
        } finally {
            close(out);
        }
    }

    /**
     * Adds an account, which is required for synchronizing with the server, if none exist.
     *
     * @param ctx      app context, used to access the account manager
     * @param username username of server account to sync with
     * @param password password for specified username
     * @return the created account, or null if no account was created
     */
    public static Account installAccount(Context ctx, String username, String password) {
        AccountManager manager = AccountManager.get(ctx);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            Log.i(TAG, "account exists: " + accounts[0].name);
        } else {
            Account account = new Account(username, ACCOUNT_TYPE);
            if (manager.addAccountExplicitly(account, password, null)) {
                Log.i(TAG, "added account " + username);
                setIsSyncable(account, AUTHORITY, 1);
                setSyncAutomatically(account, AUTHORITY, true);
                return account;
            }
            Log.w(TAG, "failed to add account");
        }
        return null;
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
        return getDefaultSharedPreferences(ctx).getBoolean(ctx.getString(R.string.use_zsync_key), true);
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
            ctx.getContentResolver().requestSync(accounts[0], AUTHORITY, new Bundle());
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
        } else {
            Log.w(TAG, "sync cancellation ignored, no account");
        }
    }

    /**
     * Downloads an SQLite database and notifies the user to apply it manually via system notifications. There are two
     * modes of downloading the SQLite file, incremental and direct. By default, incremental downloading is enabled,
     * though the user can disable it via preferences. The first time a database is downloaded, it is downloaded
     * directly. Subsequent downloads use zsync to download only differences from the local content.
     *
     * @param ctx      app context to use for locating resources like files, etc.
     * @param username username to use for http auth
     * @param password password to use for http auth
     */
    public static void downloadUpdate(final Context ctx, String username, String password) {

        File dbFile = getDatabaseFile(ctx), dbTempFile = getTempFile(dbFile);

        String existingFingerprint = loadFirstLine(getFingerprintFile(dbTempFile.exists() ? dbTempFile : dbFile));

        DatabaseAdapter db = DatabaseAdapter.getInstance(ctx);

        final NotificationManager manager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);

        String fingerprint = "?";

        boolean downloadedContentBefore = getFingerprintFile(dbFile).exists();
        boolean useZsync = isZyncEnabled(ctx) && downloadedContentBefore;

        String accept;
        if (useZsync) {
            accept = join(", ", SQLITE_MIME_TYPE, Metadata.MIME_TYPE);
        } else {
            accept = SQLITE_MIME_TYPE;
        }

        try {

            long startTime = System.currentTimeMillis();

            String creds = encodeBasicCreds(username, password);
            URL endpoint = getSyncEndpoint(ctx);
            HttpURLConnection httpConn = get(endpoint, accept, creds, existingFingerprint);
            int httpResult = httpConn.getResponseCode();

            final Notification.Builder builder = new Notification.Builder(ctx)
                    .setSmallIcon(R.drawable.ic_progress)
                    .setContentTitle(ctx.getString(R.string.sync_database_new_data))
                    .setContentText(ctx.getString(R.string.sync_database_in_progress))
                    .setProgress(0, 0, true)
                    .setOngoing(true);

            switch (httpResult) {
                case SC_NOT_MODIFIED:
                    Log.i(TAG, "no update found");
                    break;
                case SC_OK:
                    try {
                        manager.cancel(SYNC_NOTIFICATION_ID);
                        File fingerprintFile = getFingerprintFile(dbTempFile);
                        fingerprint = httpConn.getHeaderField("ETag");
                        Log.i(TAG, "update " + fingerprint + " found, fetching");
                        if (fingerprintFile.exists()) {
                            if (!fingerprintFile.delete()) {
                                Log.w(TAG, "failed to clear old fingerprint, user could install partial content!");
                                ctx.getContentResolver().notifyChange(OpenHDS.CONTENT_BASE_URI, null, false);
                            }
                        }

                        manager.notify(SYNC_NOTIFICATION_ID, builder.getNotification());

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
                            incrementalSync(responseBody, dbTempFile, scratch, factory, builder, manager, ctx);
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
                                        manager.notify(SYNC_NOTIFICATION_ID, builder.getNotification());
                                    }
                                    fileProgress = nextValue;
                                }
                            };
                            streamToFile(responseBody, dbTempFile, totalSize == -1? null : progressListener);
                        }

                        store(fingerprintFile, fingerprint);  // install fingerprint after downloaded finishes
                        Log.i(TAG, "database downloaded");
                        db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "success");
                        Intent intent = new Intent(ctx, SupervisorActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pending = PendingIntent.getActivity(ctx, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        manager.notify(SYNC_NOTIFICATION_ID, builder
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentText(ctx.getString(R.string.sync_database_new_data_instructions))
                                .setContentIntent(pending)
                                .setProgress(0, 0, false)
                                .setOngoing(false)
                                .getNotification());
                    } catch (IOException | NoSuchAlgorithmException e) {
                        Log.e(TAG, "sync io failure", e);
                        db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "error: " + httpResult);
                        manager.notify(SYNC_NOTIFICATION_ID, builder
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentText(ctx.getString(R.string.sync_database_failed))
                                .setProgress(0, 0, false)
                                .setOngoing(false)
                                .getNotification());
                    } catch (InterruptedException e) {
                        Log.e(TAG, "sync thread canceled", e);
                        db.addSyncResult(fingerprint, startTime, System.currentTimeMillis(), "canceled");
                        manager.cancel(SYNC_NOTIFICATION_ID);
                    }
                    break;
                default:
                    Log.i(TAG, "unexpected status code " + httpResult);
            }
        } catch (IOException e) {
            Log.e(TAG, "sync failed: " + e.getMessage());
        }
        db.pruneSyncResults(getHistoryRetention(ctx));
        ctx.getContentResolver().notifyChange(OpenHDS.CONTENT_BASE_URI, null, false);
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
                                        final Notification.Builder builder, final NotificationManager manager,
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
                    manager.notify(SYNC_NOTIFICATION_ID, builder.getNotification());
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
                OpenHDSProvider.getDatabaseHelper(ctx).close();
                listener.installed();
            } else {
                Log.e(TAG, "failed to install update");
            }
        }
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
        public void close() throws IOException {
            c.disconnect();
        }
    }
}
