package org.openhds.mobile.search;

import android.database.Cursor;

import org.apache.lucene.document.Field;

class SimpleCursorDocumentSource extends CursorDocumentSource {

    SimpleCursorDocumentSource(Cursor c) {
        super(c);
    }

    @Override
    public Field[] getFields() {
        int columns = cursor.getColumnCount();
        Field[] fields = new Field[columns];
        for (int c = 0; c < columns; c++) {
            boolean dataCol = c <= 1;  // first two columns are assumed to be uuid and level
            String columnName = cursor.getColumnName(c);
            fields[c] = new Field(columnName, "",
                    dataCol ? Field.Store.YES : Field.Store.NO,
                    dataCol ? Field.Index.NO : Field.Index.ANALYZED);
            document.add(fields[c]);
        }
        return fields;
    }

    @Override
    public String getFieldValue(int index) {
        return cursor.getString(index);
    }
}
