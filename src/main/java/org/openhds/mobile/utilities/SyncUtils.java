package org.openhds.mobile.utilities;

import android.util.Log;

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

import static com.github.batkinson.jrsync.zsync.IOUtil.BUFFER_SIZE;
import static com.github.batkinson.jrsync.zsync.IOUtil.buffer;
import static com.github.batkinson.jrsync.zsync.IOUtil.close;

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
