package org.cimsbioko.repository;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class CursorConvert {

    public static <X> List<X> list(Cursor cursor, CursorConverter<X> converter) {
        List<X> result = new ArrayList<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    result.add(converter.convert(cursor));
                }
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    public static <Y> Y one(Cursor cursor, CursorConverter<Y> converter) {
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return converter.convert(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

}
