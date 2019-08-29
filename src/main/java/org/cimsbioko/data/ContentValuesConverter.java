package org.cimsbioko.data;

import android.content.ContentValues;

/**
 * Convert an entity to database content values and convert a database cursor to an entity.
 */
public interface ContentValuesConverter<T> {
    ContentValues toContentValues(T entity);
}
