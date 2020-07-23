package org.cimsbioko.data

import android.database.Cursor

interface CursorConverter<T> {
    fun convert(c: Cursor): T
}