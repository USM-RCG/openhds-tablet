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
            fields[c] = new Field(getFieldName(c), "", getFieldStore(c), getFieldIndex(c));
        }
        return fields;
    }

    public String getFieldName(int index) {
        return cursor.getColumnName(index);
    }

    public Field.Store getFieldStore(int index) {
        return index == UUID_COL_IDX || index == LEVEL_COL_IDX ? Field.Store.YES : Field.Store.NO;
    }

    public Field.Index getFieldIndex(int index) {
        if (index == UUID_COL_IDX) {
            return Field.Index.NO;
        } else if (index == LEVEL_COL_IDX) {
            return Field.Index.NOT_ANALYZED;
        } else {
            return Field.Index.ANALYZED;
        }
    }

    @Override
    public String getFieldValue(int index) {
        return cursor.getString(index);
    }
}
