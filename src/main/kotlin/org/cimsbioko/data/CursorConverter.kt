package org.cimsbioko.data

import android.database.Cursor

interface CursorConverter<T : Any> {
    fun convert(c: Cursor): T
}