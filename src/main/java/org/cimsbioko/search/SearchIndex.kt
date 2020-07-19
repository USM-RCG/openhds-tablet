package org.cimsbioko.search

import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.SearcherFactory
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.cimsbioko.App
import java.io.File
import java.io.IOException

object SearchIndex {

    private val searcherManager by lazy {
        val indexFile = File(App.getApp().applicationContext.filesDir, "search-index")
        val indexDir: Directory = FSDirectory.open(indexFile)
        SearcherManager(indexDir, SearcherFactory())
    }

    @Throws(IOException::class)
    fun acquire(): IndexSearcher = with(searcherManager) {
        if (!isSearcherCurrent) {
            maybeRefresh()
        }
        return acquire()
    }


    @Throws(IOException::class)
    fun IndexSearcher.release() {
        searcherManager.release(this)
    }
}