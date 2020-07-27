package org.cimsbioko.search

import android.util.Log
import org.apache.lucene.search.IndexSearcher
import org.cimsbioko.search.SearchIndex.acquire
import org.cimsbioko.search.SearchIndex.release

abstract class SearchJob : Runnable {

    internal var service: SearchQueue? = null

    private val isActive: Boolean
        get() = !(service?.isShutdown ?: true)

    override fun run() {
        if (isActive) {
            try {
                with(acquire()) {
                    try {
                        performSearch()
                        if (isActive) {
                            postResult()
                        }
                    } finally {
                        release()
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    handleException(e)
                }
            }
        }
    }

    @Throws(Exception::class)
    abstract fun IndexSearcher.performSearch()

    @Throws(Exception::class)
    protected abstract fun postResult()

    open fun handleException(e: Exception) {
        Log.e(TAG, "search job failed", e)
    }

    companion object {
        private val TAG = SearchJob::class.java.simpleName
    }
}