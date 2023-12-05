package org.cimsbioko.search

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.app.NotificationCompat
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.Term
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.cimsbioko.App
import org.cimsbioko.R
import org.cimsbioko.navconfig.NavigatorConfig
import org.cimsbioko.provider.ContentProvider
import org.cimsbioko.utilities.NotificationUtils.PROGRESS_NOTIFICATION_RATE_MILLIS
import org.cimsbioko.utilities.NotificationUtils.SYNC_CHANNEL_ID
import org.cimsbioko.utilities.NotificationUtils.getNotificationColor
import org.cimsbioko.utilities.NotificationUtils.getNotificationManager
import org.cimsbioko.utilities.NotificationUtils.notificationIcon
import java.io.File
import java.io.IOException

class Indexer private constructor() {

    private val indexFile = File(App.instance.applicationContext.filesDir, "search-index")

    /* convenient access to the content provider's database, do not close the returned database! */
    private val database: SQLiteDatabase
        get() = ContentProvider.databaseHelper.readableDatabase

    @get:Throws(IOException::class)
    private val writer: IndexWriter
        get() {
            val indexDir: Directory = FSDirectory.open(indexFile)
            val analyzer: Analyzer = CustomAnalyzer()
            val config = IndexWriterConfig(Version.LUCENE_47, analyzer).apply {
                openMode = OpenMode.CREATE_OR_APPEND
            }
            return IndexWriter(indexDir, config)
        }

    fun reindexAll() {
        try {
            writer.use {
                with(it) {
                    deleteAll()
                    bulkIndexHierarchy()
                    bulkIndexLocations()
                    bulkIndexIndividuals()
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "io error, indexing failed: " + e.message)
        }
    }

    private fun EntityType.reindexEntity(uuid: String) {
        getDocSource(isBulk = false, uuid)?.let { docSource ->
            writer.use {
                with(it) {
                    try {
                        updateIndex(docSource, entityId)
                    } finally {
                        commit()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    fun reindexHierarchy(uuid: String) = EntityType.HIERARCHY.reindexEntity(uuid)

    @Throws(IOException::class)
    fun reindexLocation(uuid: String) = EntityType.LOCATION.reindexEntity(uuid)

    @Throws(IOException::class)
    fun reindexIndividual(uuid: String) = EntityType.INDIVIDUAL.reindexEntity(uuid)

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndexHierarchy() {
        EntityType.HIERARCHY.getDocSource()?.let { bulkIndex(R.string.indexing_hierarchy_items, it) }
    }

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndexLocations() {
        EntityType.LOCATION.getDocSource()?.let { bulkIndex(R.string.indexing_locations, it) }
    }

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndexIndividuals() {
        EntityType.INDIVIDUAL.getDocSource()?.let { bulkIndex(R.string.indexing_individuals, it) }
    }

    private fun EntityType.getDocSource(isBulk: Boolean = true, vararg queryArgs: String): DocumentSource? {
        return NavigatorConfig.instance.searchSources[configName]?.let { source ->
            var query: String? = source.query
            var args = emptyArray<String>()
            if (!isBulk) {
                query = source.fields.firstOrNull { it.name == entityId }?.let { "${source.query} where $it = ?" }
                args = arrayOf(*queryArgs)
            }
            query?.let { CampaignDocumentSource(source, database.rawQuery(query, args)) }
        }.also { if (it == null) Log.i(TAG, "failed to find search source for $configName") }
    }

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndex(label: Int, source: DocumentSource) {
        val ctx = App.instance.applicationContext
        val notificationManager = getNotificationManager(ctx)
        val notificationBuilder = NotificationCompat.Builder(ctx, SYNC_CHANNEL_ID)
                .setSmallIcon(notificationIcon)
                .setColor(getNotificationColor(ctx))
                .setContentTitle(ctx.getString(R.string.updating_index))
                .setContentText(ctx.getString(label))
                .setOngoing(true)
        var lastUpdate: Long = 0
        source.use {
            with(it) {
                try {
                    if (next()) {
                        val totalCount = size()
                        var processed = 0
                        var lastNotified = -1
                        do {
                            addDocument(document)
                            processed++
                            val thisUpdate = System.currentTimeMillis()
                            if (thisUpdate - lastUpdate > PROGRESS_NOTIFICATION_RATE_MILLIS) {
                                val percentFinished = (processed / totalCount.toFloat() * 100).toInt()
                                if (lastNotified != percentFinished) {
                                    notificationBuilder.setProgress(totalCount, processed, false)
                                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                                    lastNotified = percentFinished
                                    lastUpdate = thisUpdate
                                }
                            }
                        } while (next())
                    }
                } finally {
                    notificationManager.cancel(NOTIFICATION_ID)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun IndexWriter.updateIndex(source: DocumentSource, idField: String) {
        source.use {
            with(it) {
                if (next()) {
                    do {
                        val doc = document
                        val idTerm = Term(idField, doc[idField])
                        updateDocument(idTerm, doc)
                    } while (next())
                }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 13
        private val TAG = IndexingService::class.java.simpleName
        val instance by lazy { Indexer() }
    }
}