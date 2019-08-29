package org.cimsbioko.repository;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Convert an entity to database content values and convert a database cursor to an entity.
 */
public interface Converter<T> {

    T toEntity(Cursor cursor);

    ContentValues toContentValues(T entity);

    String getId(T entity);

    DataWrapper toWrapper(Cursor cursor, String level);

}
