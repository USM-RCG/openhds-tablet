package org.openhds.mobile.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Utilities to build queries and facilitate CRUD operations.
 *
 * BSH
 */
public class RepositoryUtils {

    public static final String EQUALS = "=";
    public static final String LIKE = "LIKE";
    public static final String LIKE_WILD_CARD = "%";

    private static final String AND = "AND";
    private static final String LIMIT = "LIMIT";
    private static final String OFFSET = "OFFSET";
    private static final String WHERE_PLACEHOLDER = "?";
    private static final String WHERE_ALL = "1";

    public static Uri insert(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues) {
        return contentResolver.insert(tableUri, contentValues);
    }

    public static int update(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues,
                             String columnName, String columnValue) {

        final String[] columnNames = {columnName};
        final String[] columnValues = {columnValue};
        return update(contentResolver, tableUri, contentValues, columnNames, columnValues);
    }

    public static int update(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues,
                             String[] columnNames, String[] columnValues) {

        final String whereStatement = buildWhereStatement(columnNames, EQUALS);
        return contentResolver.update(tableUri, contentValues, whereStatement, columnValues);
    }

    public static Cursor query(ContentResolver contentResolver, Uri tableUri, String whereStatement,
                               String[] columnValues, String columnOrderBy) {
        return contentResolver.query(tableUri, null, whereStatement, columnValues, columnOrderBy);
    }

    public static Cursor queryRange(ContentResolver contentResolver, Uri tableUri, String whereStatement,
                                    String[] columnValues, String columnOrderBy, int start, int maxResults) {

        final String rangeStatement = buildRangeStatement(start, maxResults);
        final String orderByPlusRange = columnOrderBy + " " + rangeStatement;
        return contentResolver.query(tableUri, null, whereStatement, columnValues, orderByPlusRange);
    }

    public static String buildWhereStatement(String[] columnNames, String operator) {
        if (null == columnNames || 0 == columnNames.length) {
            return WHERE_ALL;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(buildWhereClause(columnNames[0], operator));

        if (1 == columnNames.length) {
            return stringBuilder.toString();
        }

        for (int i = 1; i < columnNames.length; i++) {
            stringBuilder.append(" " + AND + " ");
            stringBuilder.append(buildWhereClause(columnNames[i], operator));
        }

        return stringBuilder.toString();
    }

    private static String buildWhereClause(String columnName, String operator) {
        return columnName + " " + operator + " " + WHERE_PLACEHOLDER;
    }

    /*
     * This is a poor way to do scrolling. It will be slower the deeper you get into results and
     * it will consume far more power than it should. The right way to do this is to compare keys
     * on an indexed field.
     *
     * For details, see: http://www.sqlite.org/cvstrac/wiki?p=ScrollingCursor
     */
    public static String buildRangeStatement(int start, int maxResults) {
        return LIMIT + " " + maxResults + " " + OFFSET + " " + start;
    }

    public static String extractString(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex < 0) {
            return null;
        }
        return cursor.getString(columnIndex);
    }

    public static int extractInt(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex < 0) {
            return 0;
        }
        return cursor.getInt(columnIndex);
    }
}

