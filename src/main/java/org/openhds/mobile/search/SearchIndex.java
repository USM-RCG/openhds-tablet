package org.openhds.mobile.search;

import android.content.Context;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;


public class SearchIndex {

    private static SearchIndex INSTANCE;

    private Context ctx;
    private SearcherManager searcherManager;

    private SearchIndex(Context ctx) {
        this.ctx = ctx;
    }

    public static SearchIndex getInstance(Context ctx) {
        if (INSTANCE == null) {
            INSTANCE = new SearchIndex(ctx.getApplicationContext());
        }
        return INSTANCE;
    }

    public IndexSearcher acquire() throws IOException {
        if (searcherManager == null) {
            File indexFile = new File(ctx.getFilesDir(), "search-index");
            Directory indexDir = FSDirectory.open(indexFile);
            searcherManager = new SearcherManager(indexDir, new SearcherFactory());
        } else if (!searcherManager.isSearcherCurrent()) {
            searcherManager.maybeRefresh();
        }
        return searcherManager.acquire();
    }

    public void release(IndexSearcher searcher) throws IOException {
        searcherManager.release(searcher);
    }
}
