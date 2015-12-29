package org.openhds.mobile.repository;

import android.content.ContentResolver;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Store a Query and return results from multiple piecewise selects.
 *
 * This allows Queries to return large result sets without having
 * to fully populate a Collection of results (which might take
 * unreasonable time and memory).  It also prevents the caller
 * from having to manage database cursors.
 *
 * BSH
 */
public class ResultsIterator<T> implements Iterator<T> {

    public static final int DEFAULT_WINDOW_MAX_SIZE = 100;

    private final ContentResolver contentResolver;
    private final Query query;
    private final Converter<T> converter;

    private final int windowMaxSize;
    private final List<T> windowResults;
    private int windowIndex;
    private int windowQueryOffset;

    private boolean gotLastResult;

    public ResultsIterator(ContentResolver contentResolver, Query query, Converter<T> converter) {
        this(contentResolver, query, converter, DEFAULT_WINDOW_MAX_SIZE);
    }

    public ResultsIterator(ContentResolver contentResolver, Query query, Converter<T> converter, int windowMaxSize) {
        this.contentResolver = contentResolver;
        this.query = query;
        this.converter = converter;
        this.windowMaxSize = windowMaxSize;

        windowResults = new ArrayList<T>();
        windowIndex = 0;
        windowQueryOffset = 0;
        gotLastResult = false;
    }

    @Override
    public boolean hasNext() {
        // get more results as needed
        if (windowIndex >= windowResults.size()) {

            // already got the last result
            if (gotLastResult) {
                windowResults.clear();
                return false;
            }

            getNextResultSet();
            windowIndex = 0;
        }

        // got results?
        return windowResults.size() > 0;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        // hasNext() implies windowIndex < windowResults.size()
        return windowResults.get(windowIndex++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    // do a piecewise select with windowMaxSize results or fewer
    private void getNextResultSet() {
        // select a range of results
        Cursor cursor = query.selectRange(contentResolver, windowQueryOffset, windowMaxSize);

        // populate the results window
        windowResults.clear();
        while(cursor.moveToNext()) {
            windowResults.add(converter.fromCursor(cursor));
        }
        cursor.close();

        // this range contains the last result
        gotLastResult = windowResults.size() < windowMaxSize;

        // select a different range next time
        windowQueryOffset += windowMaxSize;
    }
}
