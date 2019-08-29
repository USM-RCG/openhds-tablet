package org.cimsbioko.repository;

import android.database.Cursor;

/**
 * Utilities to build queries and facilitate CRUD operations.
 *
 * BSH
 */
class WhereUtils {

    static final String EQUALS = "=";
    static final String LIKE = "LIKE";

    private static final String AND = "AND";
    private static final String WHERE_PLACEHOLDER = "?";
    private static final String WHERE_ALL = "1";

    static String buildWhereStatement(String[] columnNames, String operator) {
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
}

