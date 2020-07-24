package org.cimsbioko.provider

import android.content.*
import android.content.ContentProvider
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils
import android.util.Log
import org.cimsbioko.App
import org.cimsbioko.search.IndexingService.Companion.queueReindex
import org.cimsbioko.search.IndexingService.EntityType
import org.cimsbioko.search.Utils.isSearchEnabled


class ContentProvider : ContentProvider() {

    companion object {

        const val DATABASE_NAME = "cims.db"
        const val DATABASE_VERSION = 15

        private const val TAG = "ContentProvider"

        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(App.AUTHORITY, "individuals", INDIVIDUALS)
            addURI(App.AUTHORITY, "individuals/#", INDIVIDUAL_ID)
            addURI(App.AUTHORITY, "locations", LOCATIONS)
            addURI(App.AUTHORITY, "locations/#", LOCATION_ID)
            addURI(App.AUTHORITY, "hierarchyitems", HIERARCHYITEMS)
            addURI(App.AUTHORITY, "hierarchyitems/#", HIERARCHYITEM_ID)
            addURI(App.AUTHORITY, "fieldworkers", FIELDWORKERS)
            addURI(App.AUTHORITY, "fieldworkers/#", FIELDWORKER_ID)
        }

        private val individualProjection = mapOf(
                // general individual columns
                App.Individuals.ID to App.Individuals.ID,
                App.Individuals.COLUMN_INDIVIDUAL_UUID to App.Individuals.COLUMN_INDIVIDUAL_UUID,
                App.Individuals.COLUMN_INDIVIDUAL_DOB to App.Individuals.COLUMN_INDIVIDUAL_DOB,
                App.Individuals.COLUMN_INDIVIDUAL_EXTID to App.Individuals.COLUMN_INDIVIDUAL_EXTID,
                App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME to App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME,
                App.Individuals.COLUMN_INDIVIDUAL_GENDER to App.Individuals.COLUMN_INDIVIDUAL_GENDER,
                App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME to App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME,
                App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID to App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID,
                App.Individuals.COLUMN_INDIVIDUAL_ATTRS to App.Individuals.COLUMN_INDIVIDUAL_ATTRS,
                App.Individuals.COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD to App.Individuals.COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD,
                // extensions for bioko project
                App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES to App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES,
                App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER to App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER,
                App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER to App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER,
                App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME to App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME,
                App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER to App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER,
                App.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE to App.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE,
                App.Individuals.COLUMN_INDIVIDUAL_STATUS to App.Individuals.COLUMN_INDIVIDUAL_STATUS,
                App.Individuals.COLUMN_INDIVIDUAL_NATIONALITY to App.Individuals.COLUMN_INDIVIDUAL_NATIONALITY,
                App.Individuals.COLUMN_INDIVIDUAL_OTHER_ID to App.Individuals.COLUMN_INDIVIDUAL_OTHER_ID
        )

        private val locationProjection = mapOf(
                App.Locations.ID to App.Locations.ID,
                App.Locations.COLUMN_LOCATION_EXTID to App.Locations.COLUMN_LOCATION_EXTID,
                App.Locations.COLUMN_LOCATION_UUID to App.Locations.COLUMN_LOCATION_UUID,
                App.Locations.COLUMN_LOCATION_HIERARCHY_UUID to App.Locations.COLUMN_LOCATION_HIERARCHY_UUID,
                App.Locations.COLUMN_LOCATION_LATITUDE to App.Locations.COLUMN_LOCATION_LATITUDE,
                App.Locations.COLUMN_LOCATION_LONGITUDE to App.Locations.COLUMN_LOCATION_LONGITUDE,
                App.Locations.COLUMN_LOCATION_NAME to App.Locations.COLUMN_LOCATION_NAME,
                App.Locations.COLUMN_LOCATION_DESCRIPTION to App.Locations.COLUMN_LOCATION_DESCRIPTION,
                App.Locations.COLUMN_LOCATION_ATTRS to App.Locations.COLUMN_LOCATION_ATTRS
        )

        private val hierarchyProjection = mapOf(
                App.HierarchyItems.ID to App.HierarchyItems.ID,
                App.HierarchyItems.COLUMN_HIERARCHY_EXTID to App.HierarchyItems.COLUMN_HIERARCHY_EXTID,
                App.HierarchyItems.COLUMN_HIERARCHY_UUID to App.HierarchyItems.COLUMN_HIERARCHY_UUID,
                App.HierarchyItems.COLUMN_HIERARCHY_LEVEL to App.HierarchyItems.COLUMN_HIERARCHY_LEVEL,
                App.HierarchyItems.COLUMN_HIERARCHY_NAME to App.HierarchyItems.COLUMN_HIERARCHY_NAME,
                App.HierarchyItems.COLUMN_HIERARCHY_PARENT to App.HierarchyItems.COLUMN_HIERARCHY_PARENT,
                App.HierarchyItems.COLUMN_HIERARCHY_ATTRS to App.HierarchyItems.COLUMN_HIERARCHY_ATTRS
        )

        private val fieldworkerProjection = mapOf(
                App.FieldWorkers.ID to App.FieldWorkers.ID,
                App.FieldWorkers.COLUMN_FIELD_WORKER_EXTID to App.FieldWorkers.COLUMN_FIELD_WORKER_EXTID,
                App.FieldWorkers.COLUMN_FIELD_WORKER_UUID to App.FieldWorkers.COLUMN_FIELD_WORKER_UUID,
                App.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX to App.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX,
                App.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME to App.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME,
                App.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME to App.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME,
                App.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD to App.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD
        )

        private const val INDIVIDUALS = 1
        private const val INDIVIDUAL_ID = 2
        private const val LOCATIONS = 3
        private const val LOCATION_ID = 4
        private const val HIERARCHYITEMS = 5
        private const val HIERARCHYITEM_ID = 6
        private const val FIELDWORKERS = 13
        private const val FIELDWORKER_ID = 14

        val databaseHelper: DatabaseHelper by lazy { DatabaseHelper(App.instance.applicationContext) }
    }

    /**
     * Initializes the provider by creating a new DatabaseHelper. onCreate() is
     * called automatically when Android creates the provider in response to a
     * resolver request from a client.
     */
    override fun onCreate(): Boolean = true

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int = with(databaseHelper.writableDatabase) {
        beginTransaction()
        try {
            return super.bulkInsert(uri, values).also { setTransactionSuccessful() }
        } finally {
            endTransaction()
        }
    }

    override fun query(
            uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor = with(SQLiteQueryBuilder()) {
        when (sUriMatcher.match(uri)) {
            INDIVIDUALS -> {
                tables = App.Individuals.TABLE_NAME
                setProjectionMap(individualProjection)
            }
            INDIVIDUAL_ID -> {
                tables = App.Individuals.TABLE_NAME
                setProjectionMap(individualProjection)
                appendWhere("${App.Individuals.ID} = ${uri.pathSegments[1]}")
            }
            LOCATIONS -> {
                tables = App.Locations.TABLE_NAME
                setProjectionMap(locationProjection)
            }
            LOCATION_ID -> {
                tables = App.Locations.TABLE_NAME
                setProjectionMap(locationProjection)
                appendWhere("${App.Locations.ID} = ${uri.pathSegments[1]}")
            }
            HIERARCHYITEMS -> {
                tables = App.HierarchyItems.TABLE_NAME
                setProjectionMap(hierarchyProjection)
            }
            HIERARCHYITEM_ID -> {
                tables = App.HierarchyItems.TABLE_NAME
                setProjectionMap(hierarchyProjection)
                appendWhere("${App.HierarchyItems.ID} = ${uri.pathSegments[1]}")
            }
            FIELDWORKERS -> {
                tables = App.FieldWorkers.TABLE_NAME
                setProjectionMap(fieldworkerProjection)
            }
            FIELDWORKER_ID -> {
                tables = App.FieldWorkers.TABLE_NAME
                setProjectionMap(fieldworkerProjection)
                appendWhere("${App.FieldWorkers.ID} = ${uri.pathSegments[1]}")
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        query(databaseHelper.readableDatabase,  // The database to query
                projection,  // The columns to return from the query
                selection,  // The columns for the where clause
                selectionArgs,  // The values for the where clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                if (TextUtils.isEmpty(sortOrder)) App.DEFAULT_SORT_ORDER else sortOrder // The sort order
        ).apply {
            setNotificationUri(context!!.contentResolver, uri)
        }
    }

    override fun getType(uri: Uri): String {
        return when (sUriMatcher.match(uri)) {
            INDIVIDUALS -> App.Individuals.CONTENT_TYPE
            INDIVIDUAL_ID -> App.Individuals.CONTENT_ITEM_TYPE
            LOCATIONS -> App.Locations.CONTENT_TYPE
            LOCATION_ID -> App.Locations.CONTENT_ITEM_TYPE
            HIERARCHYITEMS -> App.HierarchyItems.CONTENT_TYPE
            HIERARCHYITEM_ID -> App.HierarchyItems.CONTENT_ITEM_TYPE
            FIELDWORKERS -> App.FieldWorkers.CONTENT_TYPE
            FIELDWORKER_ID -> App.FieldWorkers.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    override fun insert(uri: Uri, initialValues: ContentValues): Uri {
        val table: String
        val contentUriBase: Uri
        var reindexType: EntityType? = null
        var uuid: String? = null
        when (sUriMatcher.match(uri)) {
            INDIVIDUALS -> {
                table = App.Individuals.TABLE_NAME
                contentUriBase = App.Individuals.CONTENT_ID_URI_BASE
                reindexType = EntityType.INDIVIDUAL
                uuid = initialValues.getAsString(App.Individuals.COLUMN_INDIVIDUAL_UUID)
            }
            LOCATIONS -> {
                table = App.Locations.TABLE_NAME
                contentUriBase = App.Locations.CONTENT_ID_URI_BASE
                reindexType = EntityType.LOCATION
                uuid = initialValues.getAsString(App.Locations.COLUMN_LOCATION_UUID)
            }
            HIERARCHYITEMS -> {
                table = App.HierarchyItems.TABLE_NAME
                contentUriBase = App.HierarchyItems.CONTENT_ID_URI_BASE
                reindexType = EntityType.HIERARCHY
                uuid = initialValues.getAsString(App.HierarchyItems.COLUMN_HIERARCHY_UUID)
            }
            FIELDWORKERS -> {
                table = App.FieldWorkers.TABLE_NAME
                contentUriBase = App.FieldWorkers.CONTENT_ID_URI_BASE
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        val values = ContentValues(initialValues)
        val rowId = databaseHelper.writableDatabase.insert(table, null, values)
        if (rowId > 0) {
            val ctx = context!!
            val noteUri = ContentUris.withAppendedId(contentUriBase, rowId)
            if (reindexType != null && uuid != null && isSearchEnabled(ctx)) {
                queueReindex(ctx, reindexType, uuid)
            }
            return noteUri.also { ctx.contentResolver.notifyChange(it, null) }
        }
        throw SQLException("Failed to insert row into $uri for content $values")
    }

    override fun delete(uri: Uri, where: String, whereArgs: Array<String>): Int = with(databaseHelper.writableDatabase) {
        val finalWhere: String
        when (sUriMatcher.match(uri)) {
            INDIVIDUALS -> delete(App.Individuals.TABLE_NAME, where, whereArgs)
            INDIVIDUAL_ID -> {
                finalWhere = buildFinalWhere(uri, 1, where)
                delete(App.Individuals.TABLE_NAME, finalWhere, whereArgs)
            }
            LOCATIONS -> delete(App.Locations.TABLE_NAME, where, whereArgs)
            LOCATION_ID -> {
                finalWhere = buildFinalWhere(uri, 1, where)
                delete(App.Locations.TABLE_NAME, finalWhere, whereArgs)
            }
            HIERARCHYITEMS -> delete(App.HierarchyItems.TABLE_NAME, where, whereArgs)
            HIERARCHYITEM_ID -> {
                finalWhere = buildFinalWhere(uri, 1, where)
                delete(App.HierarchyItems.TABLE_NAME, finalWhere, whereArgs)
            }
            FIELDWORKERS -> delete(App.FieldWorkers.TABLE_NAME, where, whereArgs)
            FIELDWORKER_ID -> {
                finalWhere = buildFinalWhere(uri, 1, where)
                delete(App.FieldWorkers.TABLE_NAME, finalWhere, whereArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }.also { context!!.contentResolver.notifyChange(uri, null) }

    private fun buildFinalWhere(uri: Uri, pathPosition: Int, where: String?): String = buildString {
        append("${BaseColumns._ID} = ${uri.pathSegments[pathPosition]}")
        if (where != null) {
            append(" AND $where")
        }
    }

    override fun update(uri: Uri, values: ContentValues, where: String, whereArgs: Array<String>): Int {
        val db = databaseHelper.writableDatabase
        val count: Int
        val finalWhere: String
        var reindexType: EntityType? = null
        var uuid: String? = null
        when (sUriMatcher.match(uri)) {
            INDIVIDUALS -> {
                count = db.update(App.Individuals.TABLE_NAME, values, where, whereArgs)
                reindexType = EntityType.INDIVIDUAL
                uuid = values.getAsString(App.Individuals.COLUMN_INDIVIDUAL_UUID)
            }
            INDIVIDUAL_ID -> {
                finalWhere = buildFinalWhere(uri, 1, where)
                count = db.update(App.Individuals.TABLE_NAME, values, finalWhere, whereArgs)
                reindexType = EntityType.INDIVIDUAL
                uuid = values.getAsString(App.Individuals.COLUMN_INDIVIDUAL_UUID)
            }
            LOCATIONS -> {
                count = db.update(App.Locations.TABLE_NAME, values, where, whereArgs)
                reindexType = EntityType.LOCATION
                uuid = values.getAsString(App.Locations.COLUMN_LOCATION_UUID)
            }
            LOCATION_ID -> {
                finalWhere = buildFinalWhere(uri, 1, where)
                count = db.update(App.Locations.TABLE_NAME, values, finalWhere, whereArgs)
                reindexType = EntityType.LOCATION
                uuid = values.getAsString(App.Locations.COLUMN_LOCATION_UUID)
            }
            HIERARCHYITEMS -> {
                count = db.update(App.HierarchyItems.TABLE_NAME, values, where, whereArgs)
                reindexType = EntityType.HIERARCHY
                uuid = values.getAsString(App.HierarchyItems.COLUMN_HIERARCHY_UUID)
            }
            HIERARCHYITEM_ID -> {
                finalWhere = buildFinalWhere(uri, 1, where)
                count = db.update(App.HierarchyItems.TABLE_NAME, values, finalWhere, whereArgs)
                reindexType = EntityType.HIERARCHY
                uuid = values.getAsString(App.HierarchyItems.COLUMN_HIERARCHY_UUID)
            }
            FIELDWORKERS -> count = db.update(App.FieldWorkers.TABLE_NAME, values, where,
                    whereArgs)
            FIELDWORKER_ID -> {
                finalWhere = buildFinalWhere(uri, 1, where)
                count = db.update(App.FieldWorkers.TABLE_NAME, values, finalWhere, whereArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        val ctx = context!!
        if (reindexType != null && uuid != null && isSearchEnabled(ctx)) {
            queueReindex(ctx, reindexType, uuid)
        }
        ctx.contentResolver.notifyChange(uri, null)
        return count
    }

    class DatabaseHelper internal constructor(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            with(db) {
                execSQL("""CREATE TABLE ${App.Individuals.TABLE_NAME} (
                           ${App.Individuals.ID} INTEGER,
                           ${App.Individuals.COLUMN_INDIVIDUAL_UUID} TEXT PRIMARY KEY NOT NULL,
                           ${App.Individuals.COLUMN_INDIVIDUAL_DOB} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_EXTID} TEXT NOT NULL,
                           ${App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME} TEXT NOT NULL,
                           ${App.Individuals.COLUMN_INDIVIDUAL_GENDER} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME} TEXT NOT NULL,
                           ${App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_STATUS} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_NATIONALITY} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_OTHER_ID} TEXT,
                           ${App.Individuals.COLUMN_INDIVIDUAL_ATTRS} TEXT);""".trimMargin())
                execSQL("CREATE INDEX INDIVIDUAL_UUID_INDEX ON ${App.Individuals.TABLE_NAME}(${App.Individuals.COLUMN_INDIVIDUAL_UUID});")
                execSQL("CREATE INDEX INDIVIDUAL_EXTID_INDEX ON ${App.Individuals.TABLE_NAME}(${App.Individuals.COLUMN_INDIVIDUAL_EXTID});")
                execSQL("CREATE INDEX INDIVIDUAL_RESIDENCY_INDEX ON ${App.Individuals.TABLE_NAME}(${App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID});")

                execSQL("""CREATE TABLE ${App.Locations.TABLE_NAME} (
                           ${App.Locations.ID} INTEGER,
                           ${App.Locations.COLUMN_LOCATION_EXTID} TEXT NOT NULL,
                           ${App.Locations.COLUMN_LOCATION_UUID} TEXT NOT NULL PRIMARY KEY,
                           ${App.Locations.COLUMN_LOCATION_HIERARCHY_UUID} TEXT NOT NULL,
                           ${App.Locations.COLUMN_LOCATION_LATITUDE} TEXT,
                           ${App.Locations.COLUMN_LOCATION_LONGITUDE} TEXT,
                           ${App.Locations.COLUMN_LOCATION_DESCRIPTION} TEXT,
                           ${App.Locations.COLUMN_LOCATION_NAME} TEXT NOT NULL,
                           ${App.Locations.COLUMN_LOCATION_ATTRS} TEXT);""".trimMargin())
                execSQL("CREATE INDEX LOCATION_EXTID_INDEX ON ${App.Locations.TABLE_NAME}(${App.Locations.COLUMN_LOCATION_EXTID});")
                execSQL("CREATE INDEX LOCATION_HIERARCHY_UUID_INDEX ON ${App.Locations.TABLE_NAME}(${App.Locations.COLUMN_LOCATION_HIERARCHY_UUID});")
                execSQL("CREATE INDEX LOCATION_UUID_INDEX ON ${App.Locations.TABLE_NAME}(${App.Locations.COLUMN_LOCATION_UUID});")

                execSQL("""CREATE TABLE ${App.HierarchyItems.TABLE_NAME} (
                           ${App.HierarchyItems.ID} INTEGER,
                           ${App.HierarchyItems.COLUMN_HIERARCHY_UUID} TEXT NOT NULL PRIMARY KEY,
                           ${App.HierarchyItems.COLUMN_HIERARCHY_EXTID} TEXT NOT NULL,
                           ${App.HierarchyItems.COLUMN_HIERARCHY_LEVEL} TEXT NOT NULL,
                           ${App.HierarchyItems.COLUMN_HIERARCHY_NAME} TEXT NOT NULL,
                           ${App.HierarchyItems.COLUMN_HIERARCHY_PARENT} TEXT NOT NULL,
                           ${App.HierarchyItems.COLUMN_HIERARCHY_ATTRS} TEXT);""".trimMargin())
                execSQL("CREATE INDEX LOCATIONHIERARCHY_PARENT_INDEX ON ${App.HierarchyItems.TABLE_NAME}(${App.HierarchyItems.COLUMN_HIERARCHY_PARENT});")
                execSQL("CREATE INDEX LOCATIONHIERARCHY_UUID_INDEX ON ${App.HierarchyItems.TABLE_NAME}(${App.HierarchyItems.COLUMN_HIERARCHY_UUID});")
                execSQL("CREATE INDEX LOCATIONHIERARCHY_EXTID_INDEX ON ${App.HierarchyItems.TABLE_NAME}(${App.HierarchyItems.COLUMN_HIERARCHY_EXTID});")

                execSQL("""CREATE TABLE ${App.FieldWorkers.TABLE_NAME} (
                           ${App.FieldWorkers.ID} INTEGER,
                           ${App.FieldWorkers.COLUMN_FIELD_WORKER_UUID} TEXT PRIMARY KEY NOT NULL,
                           ${App.FieldWorkers.COLUMN_FIELD_WORKER_EXTID} TEXT NOT NULL,
                           ${App.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX} TEXT NOT NULL,
                           ${App.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME} TEXT NOT NULL,
                           ${App.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME} TEXT NOT NULL,
                           ${App.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD} TEXT NOT NULL);""".trimMargin())
                execSQL("CREATE INDEX FIELDWORKERS_EXTID_INDEX ON ${App.FieldWorkers.TABLE_NAME}(${App.FieldWorkers.COLUMN_FIELD_WORKER_EXTID});")
                execSQL("CREATE INDEX FIELDWORKERS_UUID_INDEX ON ${App.FieldWorkers.TABLE_NAME}(${App.FieldWorkers.COLUMN_FIELD_WORKER_UUID});")
                execSQL("CREATE INDEX FIELDWORKERS_ID_PREFIX_INDEX ON ${App.FieldWorkers.TABLE_NAME}(${App.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX});")
                execSQL("CREATE INDEX FIELDWORKERS_PASSWORD_INDEX ON ${App.FieldWorkers.TABLE_NAME}(${App.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD});")
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 15) {
                throw SQLiteException("Can't upgrade database from version $oldVersion to $newVersion")
            } else {
                Log.w(TAG, "Upgrading database from version $oldVersion to $newVersion")
            }
        }
    }
}