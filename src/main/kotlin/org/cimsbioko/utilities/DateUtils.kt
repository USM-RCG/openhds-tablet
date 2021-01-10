package org.cimsbioko.utilities

import android.annotation.SuppressLint
import org.cimsbioko.navconfig.UsedByJSConfig
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object DateUtils {

    @UsedByJSConfig
    fun formatTime(date: Date?): String = date?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(it) } ?: ""

    @UsedByJSConfig
    fun formatDate(date: Date?): String = date?.let { SimpleDateFormat("yyyy-MM-dd").format(date) } ?: ""

}