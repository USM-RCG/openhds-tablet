package org.cimsbioko.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import org.cimsbioko.repository.Converter;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.repository.Query;

import java.util.ArrayList;
import java.util.List;

import static org.cimsbioko.App.getApp;
import static org.cimsbioko.repository.RepositoryUtils.*;


/**
 * Supertype for database table Gateways.  Expose and implement query and CRUD operations,
 */
public abstract class Gateway<T> {

    protected final Uri tableUri;
    protected final String idColumnName;
    protected final Converter<T> converter;

    // subclass constructor must supply implementation details
    public Gateway(Uri tableUri, String idColumnName, Converter<T> converter) {
        this.tableUri = tableUri;
        this.idColumnName = idColumnName;
        this.converter = converter;
    }

    // true if entity was inserted, false if updated
    public boolean insertOrUpdate(T entity) {
        ContentResolver resolver = getApp().getContentResolver();
        ContentValues contentValues = converter.toContentValues(entity);
        String id = converter.getId(entity);
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
        Query query = findById(id);
        return null != getFirst(query);
    }

    // get the first result from a query as an entity or null
    public T getFirst(Query query) {
        return toEntity(query.select());
    }

    // get all results from a query as a list
    public List<T> getList(Query query) {
        return toList(query.select());
    }

    // get the first result from a query as a QueryResult or null
    public DataWrapper getFirstQueryResult(Query query, String level) {
        T entity = getFirst(query);
        if (null == entity) {
            return null;
        }
        return converter.toDataWrapper(entity, level);
    }

    // get all results from a query as a list of QueryResults
    public List<DataWrapper> getQueryResultList(Query query, String level) {
        List<DataWrapper> dataWrappers = new ArrayList<>();
        Cursor cursor = query.select();
        List<T> entities = toList(cursor);
        for (T entity : entities) {
            dataWrappers.add(converter.toDataWrapper(entity, level));
        }
        return dataWrappers;
    }

    // find entities with given id
    public Query findById(String id) {
        return new Query(tableUri, idColumnName, id, idColumnName);
    }

    // find entities ordered by id, might be huge
    public Query findAll() {
        return new Query(tableUri, null, null, idColumnName);
    }

    // find entities where given columns are SQL "LIKE" corresponding value
    public Query findByCriteriaLike(String[] columnNames, String[] columnValues, String columnOrderBy) {
        return new Query(tableUri, columnNames, columnValues, columnOrderBy, LIKE);
    }

    // convert first result and close cursor
    protected T toEntity(Cursor cursor) {
        if(!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        T entity = converter.fromCursor(cursor);
        cursor.close();
        return entity;
    }

    // convert all results and close cursor
    protected List<T> toList(Cursor cursor) {
        List<T> list = new ArrayList<>();
        while(cursor.moveToNext()) {
            list.add(converter.fromCursor(cursor));
        }
        cursor.close();
        return list;
    }

}
