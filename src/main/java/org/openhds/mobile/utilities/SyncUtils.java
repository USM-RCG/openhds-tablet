package org.openhds.mobile.utilities;

import android.content.Context;
import android.util.Log;

import org.openhds.mobile.R;
import org.openhds.mobile.task.http.HttpTaskRequest;

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
import java.net.MalformedURLException;
import java.net.URL;

import static com.github.batkinson.jrsync.zsync.IOUtil.BUFFER_SIZE;
import static com.github.batkinson.jrsync.zsync.IOUtil.buffer;
import static com.github.batkinson.jrsync.zsync.IOUtil.close;
import static org.openhds.mobile.provider.OpenHDSProvider.DATABASE_NAME;
import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;

/**
 * Dumping grounds for miscellaneous sync-related functions.
 */
public class SyncUtils {

    private static final String TAG = SyncUtils.class.getName();

    public static final String SQLITE_MIME_TYPE = "application/x-sqlite3";

    public static String hashFilename(String filename) {
        return String.format("%s.etag", filename);
    }

    public static String tempFilename(String filename) {
        return String.format("%s.tmp", filename);
    }

    public static File getDatabaseFile(Context ctx) {
        return ctx.getDatabasePath(DATABASE_NAME);
    }

    public static File getDatabaseTempFile(Context ctx) {
        return ctx.getDatabasePath(tempFilename(DATABASE_NAME));
    }

    public static File getFingerprintFile(Context ctx) {
        return ctx.getDatabasePath(hashFilename(DATABASE_NAME));
    }

    public static String getFingerprint(Context ctx) {
        String content = loadHash(getFingerprintFile(ctx));
        return content != null ? content : "-";
    }

    public static HttpTaskRequest buildHttpRequest(Context ctx, String user, String pass) throws MalformedURLException {
        return new HttpTaskRequest(
                getSyncEndpoint(ctx).toExternalForm(), SQLITE_MIME_TYPE,
                user, pass,
                getFingerprint(ctx),
                getDatabaseTempFile(ctx));
    }

    public static URL getSyncEndpoint(Context ctx) throws MalformedURLException {
        String baseUrl = getPreferenceString(ctx, R.string.openhds_server_url_key, "");
        String path = getResourceString(ctx, R.string.sync_database_path);
        return new URL(baseUrl + path);
    }

    public static String loadHash(File hashFile) {
        String contentHash = null;
        if (hashFile.exists() && hashFile.canRead()) {
            try {
                InputStream in = new FileInputStream(hashFile);
                BufferedReader buf = new BufferedReader(new InputStreamReader(in));
                try {
                    contentHash = buf.readLine();
                } finally {
                    close(in);
                }
            } catch (FileNotFoundException e) {
                Log.w(TAG, "hash file not found", e);
            } catch (IOException e) {
                Log.w(TAG, "failed to read hash file", e);
            }
        }
        return contentHash;
    }

    public static void storeHash(File hashFile, String hash) {
        if (!hashFile.exists() || (hashFile.exists() && hashFile.canWrite())) {
            try {
                OutputStream out = new FileOutputStream(hashFile);
                try {
                    PrintWriter writer = new PrintWriter(out);
                    writer.println(hash == null ? "" : hash);
                    writer.flush();
                } finally {
                    close(out);
                }
            } catch (FileNotFoundException e) {
                Log.w(TAG, "hash file not found", e);
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
}
