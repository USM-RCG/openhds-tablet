package org.cimsbioko.data

import android.content.ContentValues

/**
 * Convert an entity to database content values and convert a database cursor to an entity.
 */
interface ContentValuesConverter<T> {
    fun toContentValues(entity: T): ContentValues
}