package org.openhds.mobile.utilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import org.openhds.mobile.R;
import org.openhds.mobile.provider.OpenHDSProvider;

import java.io.BufferedReader;
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

import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static com.github.batkinson.jrsync.zsync.IOUtil.BUFFER_SIZE;
import static com.github.batkinson.jrsync.zsync.IOUtil.buffer;
import static com.github.batkinson.jrsync.zsync.IOUtil.close;
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openhds.mobile.OpenHDS.AUTHORITY;
import static org.openhds.mobile.provider.OpenHDSProvider.DATABASE_NAME;
import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.HttpUtils.encodeBasicCreds;
import static org.openhds.mobile.utilities.HttpUtils.get;

/**
 * Dumping grounds for miscellaneous sync-related functions.
 */
public class SyncUtils {

    private static final String TAG = SyncUtils.class.getName();

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
        String baseUrl = getPreferenceString(ctx, R.string.openhds_server_url_key, "");
        String path = getResourceString(ctx, R.string.sync_database_path);
        return new URL(baseUrl + path);
    }

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

    /**
     * Streams the contents of the specified {@link InputStream} to a specified
     * location, always closing the stream.
     *
     * @param in stream to read contents from
     * @param f  location to write contents to
     * @throws IOException
     */
    public static void streamToFile(InputStream in, File f) throws IOException {
        OutputStream out = buffer(new FileOutputStream(f));
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buf)) >= 0) {
                out.write(buf, 0, read);
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
    public static boolean downloadedContentExists(Context ctx) {
        return getFingerprintFile(getTempFile(getDatabaseFile(ctx))).exists();
    }

    public interface DatabaseUpdateListener {
        void downloadedUpdate();
    }

    /**
     * Interface for a simple status callback when a database update is successfully applied.
     */
    public interface DatabaseInstallationListener {
        void installedUpdate();
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
        return loadFirstLine(getFingerprintFile(getDatabaseFile(ctx)));
    }

    /**
     * Sync should work like this:
     * <p/>
     * Get latest database fingerprint: coalesce(fingerprint(dbtmp), fingerprint(db))
     * If HTTP_OK:
     * remove temp fingerprint file
     * stream response to dbtmp
     * store etag as temp fingerprint
     * send notification that database can be updated (with intent to launch app)
     * <p/>
     * Add the ability to 'apply update' if temp fingerprint is present
     */
    public static void downloadUpdate(Context ctx, String username, String password, DatabaseUpdateListener listener) {

        File dbFile = getDatabaseFile(ctx), dbTempFile = getTempFile(dbFile);

        boolean downloadExists = downloadedContentExists(ctx);
        String existingFingerprint = loadFirstLine(getFingerprintFile(
                downloadExists ? getTempFile(getDatabaseFile(ctx)) : getDatabaseFile(ctx)
        ));

        try {
            String creds = encodeBasicCreds(username, password);
            HttpURLConnection httpConn = get(getSyncEndpoint(ctx), SQLITE_MIME_TYPE, creds, existingFingerprint);
            int result = httpConn.getResponseCode();
            switch (result) {
                case SC_NOT_MODIFIED:
                    Log.i(TAG, "no update found");
                    break;
                case SC_OK:
                    File fingerprintFile = getFingerprintFile(dbTempFile);
                    String fingerprint = httpConn.getHeaderField("ETag");
                    Log.i(TAG, "update " + fingerprint + " found, fetching");
                    if (fingerprintFile.exists() && !fingerprintFile.delete()) {
                        Log.w(TAG, "failed to clear old fingerprint, user could install partial content!");
                    }
                    streamToFile(httpConn.getInputStream(), dbTempFile);
                    store(fingerprintFile, fingerprint);  // install fingerprint after downloaded finishes
                    Log.i(TAG, "database downloaded");
                    listener.downloadedUpdate();
                    break;
                default:
                    Log.i(TAG, "unexpected status code " + result);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "invalid sync endpoint", e);
        } catch (IOException e) {
            Log.e(TAG, "sync io failure", e);
        }
    }

    public static void installUpdate(Context ctx, DatabaseInstallationListener listener) {
        File dbFile = getDatabaseFile(ctx), dbTempFile = getTempFile(dbFile),
                dbFpFile = getFingerprintFile(dbFile), dbTempFpFile = getFingerprintFile(dbTempFile);
        if (downloadedContentExists(ctx)) {
            if (dbTempFile.renameTo(dbFile) && dbTempFpFile.renameTo(dbFpFile)) {
                OpenHDSProvider.getDatabaseHelper(ctx).close();
                listener.installedUpdate();
            } else {
                Log.e(TAG, "failed to install update");
            }
        }
    }
}
