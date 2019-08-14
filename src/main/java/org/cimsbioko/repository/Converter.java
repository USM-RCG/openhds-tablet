package org.cimsbioko.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * Convert an entity to database content values and convert a database cursor to an entity.
 */
public interface Converter<T> {

    T fromCursor(Cursor cursor);

    ContentValues toContentValues(T entity);

    String getId(T entity);

    DataWrapper toDataWrapper(ContentResolver contentResolver, T entity, String level);

}
