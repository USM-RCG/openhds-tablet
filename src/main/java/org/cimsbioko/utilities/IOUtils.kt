package org.cimsbioko.utilities

import android.util.Log
import org.cimsbioko.App
import java.io.*

object IOUtils {
    private val TAG = IOUtils::class.java.simpleName
    private const val BUFFER_SIZE = 8192

    /**
     * Returns the dedicated directory for cims-exclusive data on external storage.
     *
     * @return a [File] indicating where cims-specific (public) files are/can be stored
     */
    @JvmStatic
    val externalDir: File
        get() = File(App.instance.getExternalFilesDir(null), "cims")

    /**
     * Copies the contents from one stream to another.
     *
     * @param source the [InputStream] to copy bytes from
     * @param target the [OutputStream] to copy bytes to
     * @throws IOException if any error occurs reading or writing
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(source: InputStream, target: OutputStream) {
        val buffer = ByteArray(BUFFER_SIZE)
        while (true) {
            val bytesRead = source.read(buffer)
            if (bytesRead == -1) break
            target.write(buffer, 0, bytesRead)
        }
    }

    /**
     * A convenience method to safely and easily close multiple closable resources. This is really only necessary until
     * the API level is high enough to use try-with-resources. It swallows any [IOException] occurring and logs
     * them at warn.
     *
     * @param closeables a list of possibly null closeable references or null
     */
    @JvmStatic
    fun close(vararg closeables: Closeable?) {
        if (closeables != null) {
            for (c in closeables) {
                if (c != null) {
                    try {
                        c.close()
                    } catch (e: IOException) {
                        Log.w(TAG, "failure during close", e)
                    }
                }
            }
        }
    }

    /**
     * A convenience method to wrap an output stream with a buffer to minimize system calls for multiple writes.
     *
     * @param toWrap the stream to wrap with a buffered stream
     * @return a [BufferedOutputStream] wrapping toWrap
     */
    fun buffer(toWrap: OutputStream?): OutputStream {
        return BufferedOutputStream(toWrap)
    }

    /**
     * A convenience method to wrap an input stream with a buffer to minimize system calls for multiple reads.
     *
     * @param toWrap the stram to wrap
     * @return a [BufferedInputStream] wrapping toWrap
     */
    @JvmStatic
    fun buffer(toWrap: InputStream?): InputStream {
        return BufferedInputStream(toWrap)
    }

    /**
     * Returns the first line of the specified file's contents.
     *
     * @param file the file to read
     * @return the first line of content as delimited by system line separator
     */
    @JvmStatic
    fun loadFirstLine(file: File): String? {
        var line: String? = null
        if (file.exists() && file.canRead()) {
            try {
                val `in`: InputStream = FileInputStream(file)
                val buf = BufferedReader(InputStreamReader(`in`))
                line = try {
                    buf.readLine()
                } finally {
                    close(`in`)
                }
            } catch (e: FileNotFoundException) {
                Log.w(TAG, "file $file not found for reading")
            } catch (e: IOException) {
                Log.w(TAG, "reading " + file + "failed", e)
            }
        }
        return line
    }

    /**
     * Stores the specified string in the specified file, creating if necessary.
     *
     * @param file the location of the file to write to
     * @param s    the string to store
     */
    @JvmStatic
    fun store(file: File, s: String?) {
        if (!file.exists() || file.exists() && file.canWrite()) {
            var out: OutputStream? = null
            try {
                out = FileOutputStream(file)
                val writer = PrintWriter(out)
                writer.println(s ?: "")
                writer.flush()
            } catch (e: FileNotFoundException) {
                Log.w(TAG, "file: $file not found for storing")
            } finally {
                close(out)
            }
        }
    }
    /**
     * Streams the contents of the specified [InputStream] to a specified
     * location, always closing the stream.
     *
     * @param in stream to read contents from
     * @param f  location to write contents to
     * @param listener observer to receive notifications of streaming events
     * @throws IOException
     * @throws InterruptedException
     */
    /**
     * Convenience method for [IOUtils.streamToFile].
     */
    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class, InterruptedException::class)
    fun streamToFile(`in`: InputStream, f: File?, listener: StreamListener? = null) {
        val out = buffer(FileOutputStream(f))
        try {
            streamToStream(`in`, out, listener)
        } finally {
            close(out)
        }
    }

    /**
     * Streams the contents of the specified [InputStream] to [OutputStream]. It does *not* close either
     * streams.
     *
     * @param in the stream to read from
     * @param out the stream to write to
     * @param listener object to use to notify status
     * @throws IOException
     * @throws InterruptedException
     */
    @JvmStatic
    @Throws(IOException::class, InterruptedException::class)
    fun streamToStream(`in`: InputStream, out: OutputStream, listener: StreamListener?) {
        val buf = ByteArray(BUFFER_SIZE)
        var read: Int
        while (`in`.read(buf).also { read = it } >= 0) {
            if (Thread.interrupted()) {
                throw InterruptedException()
            }
            out.write(buf, 0, read)
            listener?.streamed(read)
        }
    }

    /**
     * Copies the contents of one file to another.
     *
     * @param source [File] specifying location of file to copy
     * @param target [File] specifying location to copy to
     * @throws IOException
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyFile(source: File?, target: File?) {
        val sStream = FileInputStream(source)
        val tStream = FileOutputStream(target)
        val sChannel = sStream.channel
        val tChannel = tStream.channel
        val sourceSize = sChannel.size()
        var position: Long = 0
        while (position < sourceSize) {
            position += sChannel.transferTo(position, BUFFER_SIZE.toLong(), tChannel)
        }
    }

    interface StreamListener {
        fun streamed(bytes: Int)
    }
}

/**
 * Similar to kotlin's built-in use block, but works on a collection to avoid nested use blocks.
 */
inline fun <T : Closeable?, R> Collection<T>.use(body: () -> R): R {
    var t: Throwable? = null
    try {
        return body()
    } catch (e: Throwable) {
        t = e
        throw e
    } finally {
        when (t) {
            null -> forEach { it?.close() }
            else -> forEach {
                try {
                    it?.close()
                } catch (ct: Throwable) {
                    t.addSuppressed(ct)
                }
            }
        }
    }
}