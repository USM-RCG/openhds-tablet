package org.openhds.mobile.search;

import android.content.Context;
import android.util.Log;

import org.apache.lucene.search.IndexSearcher;

public abstract class SearchJob implements Runnable {

    private static final String TAG = SearchJob.class.getSimpleName();

    protected Context ctx;
    protected SearchQueue service;

    protected SearchJob(Context ctx) {
        this.ctx = ctx;
    }

    public void setQueue(SearchQueue service) {
        this.service = service;
    }

    boolean isActive() {
        return service != null && !service.isShutdown();
    }

    @Override
    public void run() {
        if (isActive()) {
            SearchIndex index = SearchIndex.getInstance(ctx);
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
