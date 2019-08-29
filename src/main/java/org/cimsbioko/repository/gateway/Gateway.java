package org.cimsbioko.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import org.cimsbioko.repository.*;

import static org.cimsbioko.App.getApp;
import static org.cimsbioko.repository.RepositoryUtils.EQUALS;
import static org.cimsbioko.repository.RepositoryUtils.buildWhereStatement;


/**
 * Supertype for database table Gateways.  Expose and implement query and CRUD operations,
 */
public abstract class Gateway<T> {

    protected final Uri tableUri;
    protected final String idColumnName;

    // subclass constructor must supply implementation details
    public Gateway(Uri tableUri, String idColumnName) {
        this.tableUri = tableUri;
        this.idColumnName = idColumnName;
    }

    // true if entity was inserted, false if updated
    public boolean insertOrUpdate(T entity) {
        ContentResolver resolver = getApp().getContentResolver();
        ContentValues contentValues = getContentValuesConverter().toContentValues(entity);
        String id = getId(entity);
        if (exists(id)) {
            final String[] columnNames = {idColumnName}, columnValues = {id};
            resolver.update(tableUri, contentValues, buildWhereStatement(columnNames, EQUALS), columnValues);
            return false;
        } else {
            return null != resolver.insert(tableUri, contentValues);
        }
    }

    // true if entity was found with given id
    public boolean exists(String id) {
        return null != findById(id).getFirst();
    }

    abstract String getId(T entity);

    public abstract CursorConverter<T> getEntityConverter();

    public abstract CursorConverter<DataWrapper> getWrapperConverter();

    abstract ContentValuesConverter<T> getContentValuesConverter();

    // find entities with given id
    public Query<T> findById(String id) {
        return new Query<>(this, tableUri, idColumnName, id, idColumnName);
    }

    // find entities ordered by id, might be huge
    public Query<T> findAll() {
        return new Query<>(this, tableUri, null, null, idColumnName);
    }
}