package org.cimsbioko.repository;

import android.content.ContentValues;

/**
 * Convert an entity to database content values and convert a database cursor to an entity.
 */
public interface Converter<T> {

    ContentValues toContentValues(T entity);

    String getId(T entity);

}
