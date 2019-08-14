package org.cimsbioko.search;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchQueue {

    private ExecutorService execService = Executors.newSingleThreadExecutor();
    private boolean shutdown;

    public void shutdown() {
        this.shutdown = true;
        execService.shutdownNow();
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void queue(SearchJob job) {
        if (!shutdown) {
            job.setQueue(this);
            execService.submit(job);
        }
    }
}
