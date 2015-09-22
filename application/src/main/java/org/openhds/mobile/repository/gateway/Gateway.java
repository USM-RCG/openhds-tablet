package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;
import org.openhds.mobile.repository.QueryResultsIterator;
import org.openhds.mobile.repository.ResultsIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE;
import static org.openhds.mobile.repository.RepositoryUtils.insert;
import static org.openhds.mobile.repository.RepositoryUtils.bulkInsert;
import static org.openhds.mobile.repository.RepositoryUtils.update;
import static org.openhds.mobile.repository.RepositoryUtils.delete;
import static org.openhds.mobile.repository.RepositoryUtils.countRecords;


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

    public Converter<T> getConverter() {
        return converter;
    }

    // true if entity was inserted, false if updated
    public boolean insertOrUpdate(ContentResolver contentResolver, T entity) {
        ContentValues contentValues = converter.toContentValues(entity);
        String id = converter.getId(entity);
        if (exists(contentResolver, id)) {
            update(contentResolver, tableUri, contentValues, idColumnName, id);
            return false;
        } else {
            return null != insert(contentResolver, tableUri, contentValues);
        }
    }

    // insert many entities, return number of new rows
    public int insertMany(ContentResolver contentResolver, List<T> entities) {
        ContentValues[] allContentValues = fromList(entities);
        return bulkInsert(contentResolver, tableUri, allContentValues);
    }

    // true if entity was deleted
    public boolean deleteById(ContentResolver contentResolver, String id){
        return delete(contentResolver, tableUri, idColumnName, id) > 0;
    }

    // clear the whole table and return rows deleted--be careful
    public int deleteAll(ContentResolver resolver) {
        return resolver.delete(tableUri, null, null);
    }

    // true if entity was found with given id
    public boolean exists(ContentResolver contentResolver, String id) {
        Query query = findById(id);
        return null != getFirst(contentResolver, query);
    }

    // count all the records in the table
    public int countAll(ContentResolver resolver) {
        return countRecords(resolver, tableUri);
    }

    // get the first result from a query as an entity or null
    public T getFirst(ContentResolver contentResolver, Query query) {
        Cursor cursor = query.select(contentResolver);
        return toEntity(cursor);
    }

    // get all results from a query as a list
    public List<T> getList(ContentResolver contentResolver, Query query) {
        Cursor cursor = query.select(contentResolver);
        return toList(cursor);
    }

    // get an iterator over all results from a query
    public Iterator<T> getIterator(ContentResolver contentResolver, Query query) {
        return new ResultsIterator<T>(contentResolver, query, converter);
    }

    // get an iterator over all results from a query, with given iterator window size
    public Iterator<T> getIterator(ContentResolver contentResolver, Query query, int windowSize) {
        return new ResultsIterator<T>(contentResolver, query, converter, windowSize);
    }

    // get the first result from a query as a QueryResult or null
    public DataWrapper getFirstQueryResult(ContentResolver contentResolver, Query query, String state) {
        T entity = getFirst(contentResolver, query);
        if (null == entity) {
            return null;
        }
        return converter.toDataWrapper(contentResolver, entity, state);
    }

    // get all results from a query as a list of QueryResults
    public List<DataWrapper> getQueryResultList(ContentResolver contentResolver, Query query, String state) {
        List<DataWrapper> dataWrappers = new ArrayList<DataWrapper>();
        Cursor cursor = query.select(contentResolver);
        List<T> entities = toList(cursor);
        for (T entity : entities) {
            dataWrappers.add(converter.toDataWrapper(contentResolver, entity, state));
        }
        return dataWrappers;
    }

    // get an iterator over all results from a query as QueryResults
    public Iterator<DataWrapper> getQueryResultIterator(ContentResolver contentResolver, Query query, String state) {
        return new QueryResultsIterator<T>(contentResolver, query, converter, state);
    }

    // get an iterator over all results from a query as QueryResults, with given iterator window size
    public Iterator<DataWrapper> getQueryResultIterator(ContentResolver contentResolver, Query query, String state, int windowSize) {
        return new QueryResultsIterator<T>(contentResolver, query, converter, state, windowSize);
    }

    // find entities with given id
    public Query findById(String id) {
        return new Query(tableUri, idColumnName, id, idColumnName);
    }

    // find entities ordered by id, might be huge
    public Query findAll() {
        return new Query(tableUri, null, null, idColumnName);
    }

    // find entities where given columns equal corresponding values
    public Query findByCriteriaEqual(String[] columnNames, String[] columnValues, String columnOrderBy) {
        return new Query(tableUri, columnNames, columnValues, columnOrderBy, EQUALS);
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
        List<T> list = new ArrayList<T>();
        while(cursor.moveToNext()) {
            list.add(converter.fromCursor(cursor));
        }
        cursor.close();
        return list;
    }

    // read all entities into content values
    protected ContentValues[] fromList(List<T> entities) {
        List<ContentValues> allContentValues = new ArrayList<ContentValues>();
        for (T entity : entities) {
            allContentValues.add(converter.toContentValues(entity));
        }
        return allContentValues.toArray(new ContentValues[allContentValues.size()]);
    }
}
