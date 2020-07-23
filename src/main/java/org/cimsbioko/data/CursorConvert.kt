package org.cimsbioko.data

import android.database.Cursor

internal object CursorConvert {

    @JvmStatic
    fun <X> list(cursor: Cursor?, converter: CursorConverter<X>): List<X> =
            ArrayList<X>().apply {
                cursor?.use {
                    while (it.moveToNext()) {
                        add(converter.convert(it))
                    }
                }
            }

    @JvmStatic
    fun <Y> one(cursor: Cursor?, converter: CursorConverter<Y>): Y? =
            cursor?.use { if (it.moveToFirst()) converter.convert(it) else null }

    @JvmStatic
    fun extractString(cursor: Cursor, columnName: String): String? =
            cursor.getColumnIndex(columnName).let { if (it >= 0) cursor.getString(it) else null }

}