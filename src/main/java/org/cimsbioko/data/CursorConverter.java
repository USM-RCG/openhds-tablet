package org.cimsbioko.data;

import android.database.Cursor;

public interface CursorConverter<T> {
    T convert(Cursor c);
}
