package org.cimsbioko.search;

import android.database.Cursor;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.ArrayList;
import java.util.List;

abstract class CursorDocumentSource implements DocumentSource {

    protected final Document document;  // shared instance to reduce memory allocations during indexing
    protected final Cursor cursor;
    protected Field[] fields;
    private List<Field> nullFields;

    CursorDocumentSource(Cursor c) {
        document = new Document();
        cursor = c;
    }

    public abstract Field[] getFields();

    public abstract String getFieldValue(int index);

    @Override
    public Document getDocument() {

        // add field back to document before we start next
        if (!nullFields.isEmpty()) {
            for (Field nullField : nullFields) {
                document.add(nullField);
            }
            nullFields.clear();
        }

        // manipulate document to match next cursor record, remove fields for null values
        for (int f = 0; f < fields.length; f++) {
            Field field = fields[f];
            String value = getFieldValue(f);
            if (value != null) {
                field.setStringValue(value);
            } else {
                document.removeField(field.name());
                nullFields.add(field);
            }
        }

        return document;
    }

    @Override
    public boolean next() {
        boolean isFirst = cursor.isBeforeFirst(), hasNext = cursor.moveToNext();
        if (isFirst && hasNext) {
            fields = getFields();
            nullFields = new ArrayList<>(fields.length);
            for (Field field : fields) {
                document.add(field);
            }
        }
        return hasNext;
    }

    @Override
    public int size() {
        return cursor.getCount();
    }

    @Override
    public void close() {
        cursor.close();
    }
}
