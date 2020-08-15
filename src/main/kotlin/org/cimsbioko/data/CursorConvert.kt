package org.cimsbioko.data

import android.database.Cursor

internal object CursorConvert {

    fun <X : Any> list(cursor: Cursor?, converter: CursorConverter<X>): List<X> =
            ArrayList<X>().apply {
                cursor?.use {
                    while (it.moveToNext()) {
                        add(converter.convert(it))
                    }
                }
            }

    fun <Y : Any> one(cursor: Cursor?, converter: CursorConverter<Y>): Y? =
            cursor?.use { if (it.moveToFirst()) converter.convert(it) else null }

    fun extractString(cursor: Cursor, columnName: String): String? =
            cursor.getColumnIndex(columnName).let { if (it >= 0) cursor.getString(it) else null }

}