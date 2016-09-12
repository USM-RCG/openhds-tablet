package org.openhds.mobile.search;

import android.database.Cursor;

import org.apache.lucene.document.Field;

class SimpleCursorDocumentSource extends CursorDocumentSource {

    private static final int UUID_COL_IDX = 0;
    private static final int LEVEL_COL_IDX = 1;

    SimpleCursorDocumentSource(Cursor c) {
        super(c);
    }

    @Override
    public Field[] getFields() {
        int columns = cursor.getColumnCount();
        Field[] fields = new Field[columns];
        for (int c = 0; c < columns; c++) {
            boolean isStored = c == UUID_COL_IDX || c == LEVEL_COL_IDX;
            boolean isIndexed = c != UUID_COL_IDX;
            String columnName = cursor.getColumnName(c);
            fields[c] = new Field(columnName, "",
                    isStored ? Field.Store.YES : Field.Store.NO,
                    isIndexed ? Field.Index.ANALYZED : Field.Index.NO);
            document.add(fields[c]);
        }
        return fields;
    }

    @Override
    public String getFieldValue(int index) {
        return cursor.getString(index);
    }
}
