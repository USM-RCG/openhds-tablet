package org.cimsbioko.utilities

import android.util.Log

inline fun <T : Any?> logTime(operation: String, block: () -> T): T {
    val start = System.currentTimeMillis()
    try {
        return block()
    } finally {
        Log.d("TIMING", "$operation took ${System.currentTimeMillis() - start}ms")
    }
}