package org.openhds.mobile.repository;

import android.content.ContentResolver;

import java.util.Iterator;

/**
 * Store a Query and return results from multiple piecewise selects as QueryResults.
 *
 * This wraps the behavior of a ResultsIterator.  Instead of returning real entity
 * types, it returns generic QueryResults.
 *
 * BSH
 */
public class QueryResultsIterator<T> implements Iterator<DataWrapper> {

    private final Converter<T> converter;
    private final ContentResolver contentResolver;
    private final ResultsIterator<T> resultsIterator;
    private final String state;

    public QueryResultsIterator(ContentResolver contentResolver, Query query, Converter<T> converter, String state) {
        this.converter = converter;
        this.contentResolver = contentResolver;
        this.resultsIterator = new ResultsIterator<T>(contentResolver, query, converter);
        this.state = state;
    }

    public QueryResultsIterator(ContentResolver contentResolver, Query query, Converter<T> converter, String state, int windowMaxSize) {
        this.converter = converter;
        this.contentResolver = contentResolver;
        this.resultsIterator = new ResultsIterator<T>(contentResolver, query, converter, windowMaxSize);
        this.state = state;
    }

    @Override
    public boolean hasNext() {
        return resultsIterator.hasNext();
    }

    @Override
    public DataWrapper next() {
        return converter.toDataWrapper(contentResolver, resultsIterator.next(), state);
    }

    @Override
    public void remove() {
        resultsIterator.remove();
    }
}
