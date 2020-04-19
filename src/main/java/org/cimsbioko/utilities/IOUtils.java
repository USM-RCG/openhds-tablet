package org.cimsbioko.utilities;

import android.util.Log;
import org.cimsbioko.App;

import java.io.*;
import java.nio.channels.FileChannel;

import static android.os.Environment.getExternalStorageDirectory;

public class IOUtils {

    private static final String TAG = IOUtils.class.getSimpleName();
    private static final int BUFFER_SIZE = 8192;

    /**
     * Returns the dedicated directory for cims-exclusive data on external storage.
     *
     * @return a {@link File} indicating where cims-specific (public) files are/can be stored
     */
    public static File getExternalDir() {
        return new File(App.getApp().getExternalFilesDir(null), "cims");
    }

    /**
     * Copies the contents from one stream to another.
     *
     * @param source the {@link InputStream} to copy bytes from
     * @param target the {@link OutputStream} to copy bytes to
     * @throws IOException if any error occurs reading or writing
     */
    public static void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int bytesRead = source.read(buffer);
            if (bytesRead == -1)
                break;
            target.write(buffer, 0, bytesRead);
        }
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
    public static String loadFirstLine(File file) {
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
    public static void store(File file, String s) {
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
     * Convenience method for {@link IOUtils#streamToFile(InputStream, File, StreamListener)}.
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
    public static void streamToStream(InputStream in, OutputStream out, StreamListener listener)
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
     * Copies the contents of one file to another.
     *
     * @param source {@link File} specifying location of file to copy
     * @param target {@link File} specifying location to copy to
     * @throws IOException
     */
    public static void copyFile(File source, File target) throws IOException {
        FileInputStream sStream = new FileInputStream(source);
        FileOutputStream tStream = new FileOutputStream(target);
        FileChannel sChannel = sStream.getChannel(), tChannel = tStream.getChannel();
        long sourceSize = sChannel.size(), position = 0;
        while (position < sourceSize) {
            position += sChannel.transferTo(position, BUFFER_SIZE, tChannel);
        }
    }
}
