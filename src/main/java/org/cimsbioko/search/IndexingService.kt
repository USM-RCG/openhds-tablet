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
                val type = EntityType.valueOf(intent.getStringExtra(ENTITY_TYPE))
                val uuid = intent.getStringExtra(ENTITY_UUID)
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

        @JvmStatic
        fun queueFullReindex(ctx: Context) {
            enqueueWork(ctx.applicationContext, Intent(ctx, IndexingService::class.java))
        }

        @JvmStatic
        fun queueReindex(ctx: Context, type: EntityType, uuid: String) {
            enqueueWork(ctx.applicationContext, Intent(ctx, IndexingService::class.java).apply {
                putExtra(ENTITY_TYPE, type.toString())
                putExtra(ENTITY_UUID, uuid)
            })
        }
    }
}