package org.cimsbioko.search

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import java.io.IOException

class IndexingService : JobIntentService() {

    enum class EntityType {
        HIERARCHY, LOCATION, INDIVIDUAL
    }

    override fun onHandleWork(intent: Intent) {
        with(Indexer.instance) {
            if (intent.hasExtra(ENTITY_UUID)) {
                val type: EntityType? = intent.getStringExtra(ENTITY_TYPE)?.let { EntityType.valueOf(it) }
                val uuid: String? = intent.getStringExtra(ENTITY_UUID)
                if (type != null && uuid != null) {
                    try {
                        when (type) {
                            EntityType.HIERARCHY -> reindexHierarchy(uuid)
                            EntityType.LOCATION -> reindexLocation(uuid)
                            EntityType.INDIVIDUAL -> reindexIndividual(uuid)
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "failed during reindex", e)
                    }
                } else {
                    Log.w(TAG, "ignored indexing request, missing entity type or uuid")
                }
            } else {
                reindexAll()
            }
        }
    }

    companion object {

        private val TAG = IndexingService::class.java.simpleName
        private const val ENTITY_TYPE = "entityType"
        private const val ENTITY_UUID = "entityUuid"
        private const val JOB_ID = 0xFB

        private fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, IndexingService::class.java, JOB_ID, intent)
        }

        fun queueFullReindex(ctx: Context) {
            enqueueWork(ctx.applicationContext, Intent(ctx, IndexingService::class.java))
        }

        fun queueReindex(ctx: Context, type: EntityType, uuid: String) {
            enqueueWork(ctx.applicationContext, Intent(ctx, IndexingService::class.java).apply {
                putExtra(ENTITY_TYPE, type.toString())
                putExtra(ENTITY_UUID, uuid)
            })
        }
    }
}