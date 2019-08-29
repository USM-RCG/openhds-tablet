package org.cimsbioko.repository;

import android.database.Cursor;
import android.net.Uri;
import org.cimsbioko.repository.gateway.Gateway;

import java.util.List;

import static org.cimsbioko.App.getApp;
import static org.cimsbioko.repository.RepositoryUtils.EQUALS;
import static org.cimsbioko.repository.RepositoryUtils.buildWhereStatement;

/**
 * Represent a database query to be performed.  Might be saved and performed in pieces by an Iterator.
 */
public class Query<T> {

    private final Gateway<T> gateway;
    private final Uri tableUri;
    private final String[] columnNames;
    private final String[] columnValues;
    private final String columnOrderBy;
    private final String operator;

    // simple query on one column equals value
    public Query(Gateway<T> gateway, Uri tableUri, String columnName, String columnValue, String columnOrderBy) {
        this.gateway = gateway;
        this.tableUri = tableUri;
        this.columnNames = null == columnName ? null : new String[] {columnName};
        this.columnValues = null == columnValue ? null : new String[] {columnValue};
        this.columnOrderBy = columnOrderBy;
        this.operator = EQUALS;
    }

    public Cursor select() {
        return getApp()
                .getContentResolver()
                .query(tableUri, null, buildWhereStatement(columnNames, operator), columnValues, columnOrderBy);
    }

    // get the first result from a query as an entity or null
    public T getFirst() {
        return CursorConvert.one(select(), gateway.getEntityConverter());
    }

    // get all results from a query as a list
    public List<T> getList() {
        return CursorConvert.list(select(), gateway.getEntityConverter());
    }

    // get the first result from a query as a QueryResult or null
    public DataWrapper getFirstWrapper() {
        return CursorConvert.one(select(), gateway.getWrapperConverter());
    }

    // get all results from a query as a list of QueryResults
    public List<DataWrapper> getWrapperList() {
        return CursorConvert.list(select(), gateway.getWrapperConverter());
    }
}
