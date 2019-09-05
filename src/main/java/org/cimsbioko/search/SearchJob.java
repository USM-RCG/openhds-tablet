package org.cimsbioko.search;

import android.util.Log;

import org.apache.lucene.search.IndexSearcher;

public abstract class SearchJob implements Runnable {

    private static final String TAG = SearchJob.class.getSimpleName();

    private SearchQueue service;

    void setQueue(SearchQueue service) {
        this.service = service;
    }

    private boolean isActive() {
        return service != null && !service.isShutdown();
    }

    @Override
    public void run() {
        if (isActive()) {
            SearchIndex index = SearchIndex.getInstance();
            try {
                IndexSearcher searcher = index.acquire();
                try {
                    performSearch(searcher);
                    if (isActive()) {
                        postResult();
                    }
                } finally {
                    index.release(searcher);
                }
            } catch (Exception e) {
                if (isActive()) {
                    handleException(e);
                }
            }
        }
    }

    public abstract void performSearch(IndexSearcher searcher) throws Exception;

    protected abstract void postResult() throws Exception;

    public void handleException(Exception e) {
        Log.e(TAG, "search job failed", e);
    }
}
