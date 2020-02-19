package org.cimsbioko.search;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.IOException;

public class IndexingService extends JobIntentService {

    private static final String TAG = IndexingService.class.getSimpleName();

    private static final String ENTITY_TYPE = "entityType";
    private static final String ENTITY_UUID = "entityUuid";

    private static final int JOB_ID = 0xFB;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, IndexingService.class, JOB_ID, intent);
    }

    public enum EntityType {
        HIERARCHY,
        LOCATION,
        INDIVIDUAL
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Indexer indexer = Indexer.getInstance();
        if (intent.hasExtra(ENTITY_UUID)) {
            EntityType type = EntityType.valueOf(intent.getStringExtra(ENTITY_TYPE));
            String uuid = intent.getStringExtra(ENTITY_UUID);
            try {
                switch (type) {
                    case HIERARCHY:
                        indexer.reindexHierarchy(uuid);
                        break;
                    case LOCATION:
                        indexer.reindexLocation(uuid);
                        break;
                    case INDIVIDUAL:
                        indexer.reindexIndividual(uuid);
                        break;
                    default:
                        Log.w(TAG, "unknown entity type " + type);
                }
            } catch (IOException e) {
                Log.e(TAG, "failed during reindex", e);
            }
        } else {
            indexer.reindexAll();
        }
    }

    public static void queueFullReindex(Context ctx) {
        IndexingService.enqueueWork(ctx.getApplicationContext(), new Intent(ctx, IndexingService.class));
    }

    public static void queueReindex(Context ctx, EntityType type, String uuid) {
        Intent intent = new Intent(ctx, IndexingService.class);
        intent.putExtra(ENTITY_TYPE, type.toString());
        intent.putExtra(ENTITY_UUID, uuid);
        IndexingService.enqueueWork(ctx.getApplicationContext(), intent);
    }
}

