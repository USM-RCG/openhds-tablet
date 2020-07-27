package org.cimsbioko.utilities

import android.content.Context
import org.cimsbioko.provider.ContentProvider
import java.io.File

object FileUtils {

    /**
     * Generates a filename to use for storing the ETag header value for a file. The value generated is deterministic
     * and will always generate the same name for a specified file.
     *
     * @param filename the original file name, without preceding path
     * @return filename to store the etag value
     */
    private fun hashFilename(filename: String): String {
        return String.format("%s.etag", filename)
    }

    /**
     * Generates a filename to use for storing temporary content, which may replace the content of the original file
     * specified. The value is deterministic and will always generate the same name for the specified file.
     *
     * @param filename the original file name, without preceding path
     * @return filename to store intermediate content
     */
    private fun tempFilename(filename: String): String {
        return String.format("%s.tmp", filename)
    }

    /**
     * Returns the location of the sqlite database file used to store application data.
     *
     * @param ctx application context to use for determining database directory path
     * @return a [File] object corresponding to the application's main sqlite database
     */
    @JvmStatic
    fun getDatabaseFile(ctx: Context): File {
        return ctx.getDatabasePath(ContentProvider.DATABASE_NAME)
    }

    /**
     * Returns the location of the temp file for the specified file.
     *
     * @param original [File] object corresponding to location of original file
     * @return [File] object corresponding to the temp file location for the original
     */
    @JvmStatic
    fun getTempFile(original: File): File {
        return File(original.parentFile, tempFilename(original.name))
    }

    /**
     * Returns the location of the fingerprint file for the specified file.
     *
     * @param original [File] object corresponding to the location of the original file
     * @return [File] object corresponding to the fingerprint file for the original
     */
    @JvmStatic
    fun getFingerprintFile(original: File): File {
        return File(original.parentFile, hashFilename(original.name))
    }
}