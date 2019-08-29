package org.cimsbioko.repository;

import android.database.Cursor;

public interface CursorConverter<T> {
    T convert(Cursor c);
}
