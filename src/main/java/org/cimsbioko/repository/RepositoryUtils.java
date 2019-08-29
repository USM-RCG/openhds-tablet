package org.cimsbioko.repository;

import android.database.Cursor;

/**
 * Utilities to build queries and facilitate CRUD operations.
 *
 * BSH
 */
public class RepositoryUtils {

    public static final String EQUALS = "=";
    public static final String LIKE = "LIKE";

    private static final String AND = "AND";
    private static final String WHERE_PLACEHOLDER = "?";
    private static final String WHERE_ALL = "1";

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

    public static String extractString(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return columnIndex < 0 ? null : cursor.getString(columnIndex);
    }

    public static int extractInt(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return columnIndex < 0 ? 0 : cursor.getInt(columnIndex);
    }
}

