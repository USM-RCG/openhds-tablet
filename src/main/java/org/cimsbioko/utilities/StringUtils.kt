package org.cimsbioko.utilities

import org.cimsbioko.navconfig.UsedByJSConfig

object StringUtils {

    @JvmStatic
    fun join(separator: String, vararg strings: String) = buildString {
        for ((index, str) in strings.withIndex()) {
            append(str)
            if (index < strings.lastIndex) {
                append(separator)
            }
        }
    }

    @UsedByJSConfig
    fun isEmpty(value: String?) = value == null || value.isEmpty()

}