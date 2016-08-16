package org.openhds.mobile.search;

import android.app.IntentService;
import android.content.Intent;

public class IndexingService extends IntentService {

    public IndexingService() {
        super("indexer");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Indexer indexer = Indexer.getInstance(getApplicationContext());
        indexer.bulkIndexAll();
    }
}

