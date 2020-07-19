package org.cimsbioko.search

import java.util.concurrent.Executors

class SearchQueue {

    private val execService = Executors.newSingleThreadExecutor()

    var isShutdown = false
        private set

    fun shutdown() {
        execService.shutdownNow().also { isShutdown = true }
    }

    fun queue(job: SearchJob) {
        if (!isShutdown) {
            execService.submit(job.also { it.setQueue(this) })
        }
    }
}