package org.openhds.mobile.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;

/**
 * Represent a database query to be performed.  Might be saved and performed in pieces by an Iterator.
 */
public class Query {
    private final Uri tableUri;
    private final String[] columnNames;
    private final String[] columnValues;
    private final String columnOrderBy;
    private final String operator;

    // simple query on one column equals value
    public Query(Uri tableUri, String columnName, String columnValue, String columnOrderBy) {
        this.tableUri = tableUri;
        this.columnNames = null == columnName ? null : new String[] {columnName};
        this.columnValues = null == columnValue ? null : new String[] {columnValue};
        this.columnOrderBy = columnOrderBy;
        this.operator = EQUALS;
    }

    // full, flexible query
    public Query(Uri tableUri, String[] columnNames, String[] columnValues, String columnOrderBy, String operator) {
        this.tableUri = tableUri;
        this.columnNames = columnNames;
        this.columnValues = columnValues;
        this.columnOrderBy = columnOrderBy;
        this.operator = operator;
    }

    public Cursor select(ContentResolver contentResolver) {
        final String whereStatement = RepositoryUtils.buildWhereStatement(columnNames, operator);
        return RepositoryUtils.query(contentResolver, tableUri, whereStatement, columnValues, columnOrderBy);
    }

    public Cursor selectRange(ContentResolver contentResolver, int start, int maxResults) {
        final String whereStatement = RepositoryUtils.buildWhereStatement(columnNames, operator);
        return RepositoryUtils.queryRange(contentResolver, tableUri, whereStatement, columnValues,
                columnOrderBy, start, maxResults);
    }
}
