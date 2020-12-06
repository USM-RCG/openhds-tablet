package org.cimsbioko.utilities

import android.util.Log

inline fun <T : Any?> logTime(operation: () -> String, block: () -> T): T {
    val start = System.currentTimeMillis()
    try {
        return block()
    } finally {
        val duration = System.currentTimeMillis() - start
        Log.d("TIMING", "${operation()} took $duration ms")
    }
}

inline fun <T : Any?> logTime(operation: String, block: () -> T): T {
    val start = System.currentTimeMillis()
    try {
        return block()
    } finally {
        val duration = System.currentTimeMillis() - start
        Log.d("TIMING", "$operation took $duration ms")
    }
}