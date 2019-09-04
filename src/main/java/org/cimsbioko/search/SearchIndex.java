package org.cimsbioko.search;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

import static org.cimsbioko.App.getApp;


public class SearchIndex {

    private static SearchIndex INSTANCE;

    private SearcherManager searcherManager;

    private SearchIndex() {
    }

    public static SearchIndex getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SearchIndex();
        }
        return INSTANCE;
    }

    public IndexSearcher acquire() throws IOException {
        if (searcherManager == null) {
            File indexFile = new File(getApp().getApplicationContext().getFilesDir(), "search-index");
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
