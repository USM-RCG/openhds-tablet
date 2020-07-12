package org.cimsbioko.utilities

/**
 * Convenience methods for working with SQL queries.
 */
object SQLUtils {

    @JvmStatic
    fun makePlaceholders(len: Int) =
            if (len < 1) ""
            else buildString {
                repeat(len) { index ->
                    append("?")
                    if (index < len - 1) {
                        append(",")
                    }
                }
            }

}