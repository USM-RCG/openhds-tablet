package org.openhds.mobile.search;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class IndexingService extends IntentService {

    public IndexingService() {
        super("indexer");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Indexer.getInstance(getApplicationContext()).bulkIndexAll();
    }

    public static void queueFullBuild(Context ctx) {
        ctx.startService(new Intent(ctx, IndexingService.class));
    }
}

