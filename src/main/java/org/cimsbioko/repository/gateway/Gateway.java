package org.cimsbioko.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import org.cimsbioko.repository.*;

import java.util.List;

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
        Query query = findById(id);
        return null != getFirst(query);
    }

    abstract String getId(T entity);

    abstract CursorConverter<T> getEntityConverter();

    abstract CursorConverter<DataWrapper> getWrapperConverter(String level);

    abstract ContentValuesConverter<T> getContentValuesConverter();

    // get the first result from a query as an entity or null
    public T getFirst(Query query) {
        return CursorConvert.one(query.select(), getEntityConverter());
    }

    // get all results from a query as a list
    public List<T> getList(Query query) {
        return CursorConvert.list(query.select(), getEntityConverter());
    }

    // get the first result from a query as a QueryResult or null
    public DataWrapper getFirstQueryResult(Query query, String level) {
        return CursorConvert.one(query.select(), getWrapperConverter(level));
    }

    // get all results from a query as a list of QueryResults
    public List<DataWrapper> getQueryResultList(Query query, String level) {
        return CursorConvert.list(query.select(), getWrapperConverter(level));
    }

    // find entities with given id
    public Query findById(String id) {
        return new Query(tableUri, idColumnName, id, idColumnName);
    }

    // find entities ordered by id, might be huge
    public Query findAll() {
        return new Query(tableUri, null, null, idColumnName);
    }
}