package org.cimsbioko.search

import android.database.Cursor
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
import org.cimsbioko.navconfig.Hierarchy
import org.cimsbioko.provider.ContentProvider
import org.cimsbioko.utilities.IOUtils.close
import org.cimsbioko.utilities.NotificationUtils.PROGRESS_NOTIFICATION_RATE_MILLIS
import org.cimsbioko.utilities.NotificationUtils.SYNC_CHANNEL_ID
import org.cimsbioko.utilities.NotificationUtils.getNotificationColor
import org.cimsbioko.utilities.NotificationUtils.getNotificationManager
import org.cimsbioko.utilities.NotificationUtils.notificationIcon
import java.io.File
import java.io.IOException

class Indexer private constructor() {

    private val indexFile = File(App.getApp().applicationContext.filesDir, "search-index")
    private var writer: IndexWriter? = null
    private val database: SQLiteDatabase
        get() = ContentProvider.getDatabaseHelper(App.getApp().applicationContext).readableDatabase

    @Throws(IOException::class)
    private fun getWriter(reuse: Boolean): IndexWriter {
        val w = writer
        if (!reuse) {
            w?.let {
                close()
                writer = null
            }
        }
        return w ?: let {
            val indexDir: Directory = FSDirectory.open(indexFile)
            val analyzer: Analyzer = CustomAnalyzer()
            val config = IndexWriterConfig(Version.LUCENE_47, analyzer).apply {
                openMode = OpenMode.CREATE_OR_APPEND
            }
            IndexWriter(indexDir, config).also { writer = it }
        }
    }

    fun reindexAll() {
        try {
            getWriter(false).use {
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

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndexHierarchy() = bulkIndex(R.string.indexing_hierarchy_items,
            SimpleCursorDocumentSource(database.rawQuery(HIERARCHY_INDEX_QUERY, emptyArray()))
    )

    private fun reindexEntity(cursor: Cursor, idField: String) {
        with(getWriter(false)) {
            try {
                updateIndex(SimpleCursorDocumentSource(cursor), idField)
            } finally {
                commit()
            }
        }
    }

    @Throws(IOException::class)
    fun reindexHierarchy(uuid: String) = reindexEntity(database.rawQuery(HIERARCHY_UPDATE_QUERY, arrayOf(uuid)), App.HierarchyItems.COLUMN_HIERARCHY_UUID)

    @Throws(IOException::class)
    fun reindexLocation(uuid: String) = reindexEntity(database.rawQuery(LOCATION_UPDATE_QUERY, arrayOf(uuid)), App.Locations.COLUMN_LOCATION_UUID)

    @Throws(IOException::class)
    fun reindexIndividual(uuid: String) = reindexEntity(database.rawQuery(INDIVIDUAL_UPDATE_QUERY, arrayOf(uuid)), App.Individuals.COLUMN_INDIVIDUAL_UUID)

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndexLocations() {
        bulkIndex(R.string.indexing_locations, SimpleCursorDocumentSource(database.rawQuery(LOCATION_INDEX_QUERY, emptyArray())))
    }

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndexIndividuals() {
        bulkIndex(R.string.indexing_individuals, IndividualCursorDocumentSource(database.rawQuery(INDIVIDUAL_INDEX_QUERY, arrayOf()), "name", "phone"))
    }

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndex(label: Int, source: DocumentSource) {
        val ctx = App.getApp().applicationContext
        val notificationManager = getNotificationManager(ctx)
        val notificationBuilder = NotificationCompat.Builder(ctx, SYNC_CHANNEL_ID)
                .setSmallIcon(notificationIcon)
                .setColor(getNotificationColor(ctx))
                .setContentTitle(ctx.getString(R.string.updating_index))
                .setContentText(ctx.getString(label))
                .setOngoing(true)
        var lastUpdate: Long = 0
        try {
            if (source.next()) {
                val totalCount = source.size()
                var processed = 0
                var lastNotified = -1
                do {
                    this.addDocument(source.document)
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
                } while (source.next())
            }
        } finally {
            notificationManager.cancel(NOTIFICATION_ID)
            source.close()
        }
    }

    @Throws(IOException::class)
    private fun IndexWriter.updateIndex(source: DocumentSource, idField: String) {
        with(source) {
            try {
                if (next()) {
                    do {
                        val doc = document
                        val idTerm = Term(idField, doc[idField])
                        updateDocument(idTerm, doc)
                    } while (next())
                }
            } finally {
                close()
            }
        }
    }

    companion object {
        private const val INDIVIDUAL_INDEX_QUERY = "select ${App.Individuals.COLUMN_INDIVIDUAL_UUID}, " +
                "'${Hierarchy.INDIVIDUAL}' as level, " +
                "${App.Individuals.COLUMN_INDIVIDUAL_EXTID}, " +
                "ifnull(${App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME},'') || ' ' || ifnull(${App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES},'') || ' ' || ifnull(${App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME},'') as name, " +
                "ifnull(${App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER},'') || ' ' || ifnull(${App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER},'') || ' ' || ifnull(${App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER},'') as phone " +
                "from ${App.Individuals.TABLE_NAME}"
        private const val INDIVIDUAL_UPDATE_QUERY = "$INDIVIDUAL_INDEX_QUERY where ${App.Individuals.COLUMN_INDIVIDUAL_UUID} = ?"
        private const val LOCATION_INDEX_QUERY = "select ${App.Locations.COLUMN_LOCATION_UUID}, " +
                "'${Hierarchy.HOUSEHOLD}' as level, " +
                "${App.Locations.COLUMN_LOCATION_EXTID}, " +
                "${App.Locations.COLUMN_LOCATION_NAME} " +
                "from ${App.Locations.TABLE_NAME}"
        private const val LOCATION_UPDATE_QUERY = "$LOCATION_INDEX_QUERY where ${App.Locations.COLUMN_LOCATION_UUID} = ?"
        private const val HIERARCHY_INDEX_QUERY = "select ${App.HierarchyItems.COLUMN_HIERARCHY_UUID}, " +
                "${App.HierarchyItems.COLUMN_HIERARCHY_LEVEL} as level, " +
                "${App.HierarchyItems.COLUMN_HIERARCHY_EXTID}, " +
                "${App.HierarchyItems.COLUMN_HIERARCHY_NAME} " +
                "from ${App.HierarchyItems.TABLE_NAME}"
        private const val HIERARCHY_UPDATE_QUERY = "$HIERARCHY_INDEX_QUERY where ${App.HierarchyItems.COLUMN_HIERARCHY_UUID} = ?"
        private const val NOTIFICATION_ID = 13
        private val TAG = IndexingService::class.java.simpleName
        val instance by lazy { Indexer() }
    }

}