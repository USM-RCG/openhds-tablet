package org.cimsbioko.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import org.cimsbioko.App;
import org.cimsbioko.search.IndexingService;

import java.util.HashMap;

import static org.cimsbioko.search.Utils.isSearchEnabled;

/**
 * ContentProvider for App <br />
 * This class is based on the NotPadProvider sample in the Android SDK
 */
public class ContentProvider extends android.content.ContentProvider {

    public static final String DATABASE_NAME = "cims.db";
    public static final int DATABASE_VERSION = 14;

    private static final String TAG = "ContentProvider";

    private static HashMap<String, String> individualProjection;
    private static HashMap<String, String> locationProjection;
    private static HashMap<String, String> hierarchyProjection;
    private static HashMap<String, String> fieldworkerProjection;

    private static final int INDIVIDUALS = 1;
    private static final int INDIVIDUAL_ID = 2;
    private static final int LOCATIONS = 3;
    private static final int LOCATION_ID = 4;
    private static final int HIERARCHYITEMS = 5;
    private static final int HIERARCHYITEM_ID = 6;
    private static final int FIELDWORKERS = 13;
    private static final int FIELDWORKER_ID = 14;

    private static final UriMatcher sUriMatcher;

    static {

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(App.AUTHORITY, "individuals", INDIVIDUALS);
        sUriMatcher.addURI(App.AUTHORITY, "individuals/#", INDIVIDUAL_ID);
        sUriMatcher.addURI(App.AUTHORITY, "locations", LOCATIONS);
        sUriMatcher.addURI(App.AUTHORITY, "locations/#", LOCATION_ID);
        sUriMatcher.addURI(App.AUTHORITY, "hierarchyitems", HIERARCHYITEMS);
        sUriMatcher.addURI(App.AUTHORITY, "hierarchyitems/#", HIERARCHYITEM_ID);
        sUriMatcher.addURI(App.AUTHORITY, "fieldworkers", FIELDWORKERS);
        sUriMatcher.addURI(App.AUTHORITY, "fieldworkers/#", FIELDWORKER_ID);

        individualProjection = new HashMap<>();

        // general individual columns
        individualProjection.put(App.Individuals._ID, App.Individuals._ID);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_UUID, App.Individuals.COLUMN_INDIVIDUAL_UUID);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_DOB, App.Individuals.COLUMN_INDIVIDUAL_DOB);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_EXTID, App.Individuals.COLUMN_INDIVIDUAL_EXTID);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_GENDER, App.Individuals.COLUMN_INDIVIDUAL_GENDER);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID, App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_ATTRS, App.Individuals.COLUMN_INDIVIDUAL_ATTRS);

        // extensions for bioko project
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES, App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER, App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME, App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER, App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE, App.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_STATUS, App.Individuals.COLUMN_INDIVIDUAL_STATUS);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_NATIONALITY, App.Individuals.COLUMN_INDIVIDUAL_NATIONALITY);
        individualProjection.put(App.Individuals.COLUMN_INDIVIDUAL_OTHER_ID, App.Individuals.COLUMN_INDIVIDUAL_OTHER_ID);

        locationProjection = new HashMap<>();
        locationProjection.put(App.Locations._ID, App.Locations._ID);
        locationProjection.put(App.Locations.COLUMN_LOCATION_EXTID, App.Locations.COLUMN_LOCATION_EXTID);
        locationProjection.put(App.Locations.COLUMN_LOCATION_UUID, App.Locations.COLUMN_LOCATION_UUID);
        locationProjection.put(App.Locations.COLUMN_LOCATION_HIERARCHY_UUID, App.Locations.COLUMN_LOCATION_HIERARCHY_UUID);
        locationProjection.put(App.Locations.COLUMN_LOCATION_LATITUDE, App.Locations.COLUMN_LOCATION_LATITUDE);
        locationProjection.put(App.Locations.COLUMN_LOCATION_LONGITUDE, App.Locations.COLUMN_LOCATION_LONGITUDE);
        locationProjection.put(App.Locations.COLUMN_LOCATION_NAME, App.Locations.COLUMN_LOCATION_NAME);
        locationProjection.put(App.Locations.COLUMN_LOCATION_MAP_AREA_NAME, App.Locations.COLUMN_LOCATION_MAP_AREA_NAME);
        locationProjection.put(App.Locations.COLUMN_LOCATION_SECTOR_NAME, App.Locations.COLUMN_LOCATION_SECTOR_NAME);
        locationProjection.put(App.Locations.COLUMN_LOCATION_BUILDING_NUMBER, App.Locations.COLUMN_LOCATION_BUILDING_NUMBER);
        locationProjection.put(App.Locations.COLUMN_LOCATION_DESCRIPTION, App.Locations.COLUMN_LOCATION_DESCRIPTION);
        locationProjection.put(App.Locations.COLUMN_LOCATION_ATTRS, App.Locations.COLUMN_LOCATION_ATTRS);

        hierarchyProjection = new HashMap<>();
        hierarchyProjection.put(App.HierarchyItems._ID, App.HierarchyItems._ID);
        hierarchyProjection.put(App.HierarchyItems.COLUMN_HIERARCHY_EXTID, App.HierarchyItems.COLUMN_HIERARCHY_EXTID);
        hierarchyProjection.put(App.HierarchyItems.COLUMN_HIERARCHY_UUID, App.HierarchyItems.COLUMN_HIERARCHY_UUID);
        hierarchyProjection.put(App.HierarchyItems.COLUMN_HIERARCHY_LEVEL, App.HierarchyItems.COLUMN_HIERARCHY_LEVEL);
        hierarchyProjection.put(App.HierarchyItems.COLUMN_HIERARCHY_NAME, App.HierarchyItems.COLUMN_HIERARCHY_NAME);
        hierarchyProjection.put(App.HierarchyItems.COLUMN_HIERARCHY_PARENT, App.HierarchyItems.COLUMN_HIERARCHY_PARENT);
        hierarchyProjection.put(App.HierarchyItems.COLUMN_HIERARCHY_ATTRS, App.HierarchyItems.COLUMN_HIERARCHY_ATTRS);

        fieldworkerProjection = new HashMap<>();
        fieldworkerProjection.put(App.FieldWorkers._ID, App.FieldWorkers._ID);
        fieldworkerProjection.put(App.FieldWorkers.COLUMN_FIELD_WORKER_EXTID, App.FieldWorkers.COLUMN_FIELD_WORKER_EXTID);
        fieldworkerProjection.put(App.FieldWorkers.COLUMN_FIELD_WORKER_UUID, App.FieldWorkers.COLUMN_FIELD_WORKER_UUID);
        fieldworkerProjection.put(App.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX, App.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX);
        fieldworkerProjection.put(App.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME, App.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME);
        fieldworkerProjection.put(App.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME, App.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME);
        fieldworkerProjection.put(App.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD, App.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD);
    }

    private static DatabaseHelper dbHelper;

    public static DatabaseHelper getDatabaseHelper(Context ctx) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(ctx.getApplicationContext());
        }
        return dbHelper;
    }

    /**
     *
     * Initializes the provider by creating a new DatabaseHelper. onCreate() is
     * called automatically when Android creates the provider in response to a
     * resolver request from a client.
     */
    @Override
    public boolean onCreate() {
        // Creates a new database helper object.
        // Note: database itself isn't opened until something tries to access it,
        // and it's only created if it doesn't already exist.
        dbHelper = getDatabaseHelper(getContext());
        return true;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int inserted;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            inserted = super.bulkInsert(uri, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return inserted;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case INDIVIDUALS:
                qb.setTables(App.Individuals.TABLE_NAME);
                qb.setProjectionMap(individualProjection);
                break;
            case INDIVIDUAL_ID:
                qb.setTables(App.Individuals.TABLE_NAME);
                qb.setProjectionMap(individualProjection);
                qb.appendWhere(App.Individuals._ID
                        + "="
                        + uri.getPathSegments().get(
                        App.Individuals.NOTE_ID_PATH_POSITION));
                break;
            case LOCATIONS:
                qb.setTables(App.Locations.TABLE_NAME);
                qb.setProjectionMap(locationProjection);
                break;
            case LOCATION_ID:
                qb.setTables(App.Locations.TABLE_NAME);
                qb.setProjectionMap(locationProjection);
                qb.appendWhere(App.Locations._ID
                        + "="
                        + uri.getPathSegments().get(
                        App.Locations.NOTE_ID_PATH_POSITION));
                break;
            case HIERARCHYITEMS:
                qb.setTables(App.HierarchyItems.TABLE_NAME);
                qb.setProjectionMap(hierarchyProjection);
                break;
            case HIERARCHYITEM_ID:
                qb.setTables(App.HierarchyItems.TABLE_NAME);
                qb.setProjectionMap(hierarchyProjection);
                qb.appendWhere(App.HierarchyItems._ID
                        + "="
                        + uri.getPathSegments().get(
                        App.HierarchyItems.NOTE_ID_PATH_POSITION));

                break;
            case FIELDWORKERS:
                qb.setTables(App.FieldWorkers.TABLE_NAME);
                qb.setProjectionMap(fieldworkerProjection);
                break;
            case FIELDWORKER_ID:
                qb.setTables(App.FieldWorkers.TABLE_NAME);
                qb.setProjectionMap(fieldworkerProjection);
                qb.appendWhere(App.FieldWorkers._ID
                        + "="
                        + uri.getPathSegments().get(
                        App.FieldWorkers.ID_PATH_POSITION));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = App.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = qb.query(db, // The database to query
                projection, // The columns to return from the query
                selection, // The columns for the where clause
                selectionArgs, // The values for the where clause
                null, // don't group the rows
                null, // don't filter by row groups
                orderBy // The sort order
        );

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case INDIVIDUALS:
                return App.Individuals.CONTENT_TYPE;
            case INDIVIDUAL_ID:
                return App.Individuals.CONTENT_ITEM_TYPE;
            case LOCATIONS:
                return App.Locations.CONTENT_TYPE;
            case LOCATION_ID:
                return App.Locations.CONTENT_ITEM_TYPE;
            case HIERARCHYITEMS:
                return App.HierarchyItems.CONTENT_TYPE;
            case HIERARCHYITEM_ID:
                return App.HierarchyItems.CONTENT_ITEM_TYPE;
            case FIELDWORKERS:
                return App.FieldWorkers.CONTENT_TYPE;
            case FIELDWORKER_ID:
                return App.FieldWorkers.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        String table;
        Uri contentUriBase;
        IndexingService.EntityType reindexType = null;
        String uuid = null;

        switch (sUriMatcher.match(uri)) {
            case INDIVIDUALS:
                table = App.Individuals.TABLE_NAME;
                contentUriBase = App.Individuals.CONTENT_ID_URI_BASE;
                reindexType = IndexingService.EntityType.INDIVIDUAL;
                uuid = initialValues.getAsString(App.Individuals.COLUMN_INDIVIDUAL_UUID);
                break;
            case LOCATIONS:
                table = App.Locations.TABLE_NAME;
                contentUriBase = App.Locations.CONTENT_ID_URI_BASE;
                reindexType = IndexingService.EntityType.LOCATION;
                uuid = initialValues.getAsString(App.Locations.COLUMN_LOCATION_UUID);
                break;
            case HIERARCHYITEMS:
                table = App.HierarchyItems.TABLE_NAME;
                contentUriBase = App.HierarchyItems.CONTENT_ID_URI_BASE;
                reindexType = IndexingService.EntityType.HIERARCHY;
                uuid = initialValues.getAsString(App.HierarchyItems.COLUMN_HIERARCHY_UUID);
                break;
            case FIELDWORKERS:
                table = App.FieldWorkers.TABLE_NAME;
                contentUriBase = App.FieldWorkers.CONTENT_ID_URI_BASE;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long rowId = db.insert(table, null, values);

        if (rowId > 0) {
            Context ctx = getContext();
            Uri noteUri = ContentUris.withAppendedId(contentUriBase, rowId);
            if (reindexType != null && uuid != null && isSearchEnabled(ctx)) {
                IndexingService.queueReindex(ctx, reindexType, uuid);
            }
            ctx.getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri + " for content " + values);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String finalWhere;

        int count;

        switch (sUriMatcher.match(uri)) {
            case INDIVIDUALS:
                count = db.delete(App.Individuals.TABLE_NAME, where, whereArgs);
                break;
            case INDIVIDUAL_ID:
                finalWhere = buildFinalWhere(uri,
                        App.Individuals.NOTE_ID_PATH_POSITION, where);
                count = db.delete(App.Individuals.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            case LOCATIONS:
                count = db.delete(App.Locations.TABLE_NAME, where, whereArgs);
                break;
            case LOCATION_ID:
                finalWhere = buildFinalWhere(uri,
                        App.Locations.NOTE_ID_PATH_POSITION, where);
                count = db.delete(App.Locations.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            case HIERARCHYITEMS:
                count = db.delete(App.HierarchyItems.TABLE_NAME, where,
                        whereArgs);
                break;
            case HIERARCHYITEM_ID:
                finalWhere = buildFinalWhere(uri,
                        App.HierarchyItems.NOTE_ID_PATH_POSITION, where);
                count = db.delete(App.HierarchyItems.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            case FIELDWORKERS:
                count = db
                        .delete(App.FieldWorkers.TABLE_NAME, where, whereArgs);
                break;
            case FIELDWORKER_ID:
                finalWhere = buildFinalWhere(uri,
                        App.FieldWorkers.ID_PATH_POSITION, where);
                count = db.delete(App.FieldWorkers.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    private String buildFinalWhere(Uri uri, int pathPosition, String where) {
        String finalWhere;
        finalWhere = BaseColumns._ID + " = "
                + uri.getPathSegments().get(pathPosition);

        if (where != null) {
            finalWhere = finalWhere + " AND " + where;
        }
        return finalWhere;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        String finalWhere;
        IndexingService.EntityType reindexType = null;
        String uuid = null;

        switch (sUriMatcher.match(uri)) {
            case INDIVIDUALS:
                count = db.update(App.Individuals.TABLE_NAME, values, where, whereArgs);
                reindexType = IndexingService.EntityType.INDIVIDUAL;
                uuid = values.getAsString(App.Individuals.COLUMN_INDIVIDUAL_UUID);
                break;
            case INDIVIDUAL_ID:
                finalWhere = buildFinalWhere(uri, App.Individuals.NOTE_ID_PATH_POSITION, where);
                count = db.update(App.Individuals.TABLE_NAME, values, finalWhere, whereArgs);
                reindexType = IndexingService.EntityType.INDIVIDUAL;
                uuid = values.getAsString(App.Individuals.COLUMN_INDIVIDUAL_UUID);
                break;
            case LOCATIONS:
                count = db.update(App.Locations.TABLE_NAME, values, where, whereArgs);
                reindexType = IndexingService.EntityType.LOCATION;
                uuid = values.getAsString(App.Locations.COLUMN_LOCATION_UUID);
                break;
            case LOCATION_ID:
                finalWhere = buildFinalWhere(uri, App.Locations.NOTE_ID_PATH_POSITION, where);
                count = db.update(App.Locations.TABLE_NAME, values, finalWhere, whereArgs);
                reindexType = IndexingService.EntityType.LOCATION;
                uuid = values.getAsString(App.Locations.COLUMN_LOCATION_UUID);
                break;
            case HIERARCHYITEMS:
                count = db.update(App.HierarchyItems.TABLE_NAME, values, where, whereArgs);
                reindexType = IndexingService.EntityType.HIERARCHY;
                uuid = values.getAsString(App.HierarchyItems.COLUMN_HIERARCHY_UUID);
                break;
            case HIERARCHYITEM_ID:
                finalWhere = buildFinalWhere(uri, App.HierarchyItems.NOTE_ID_PATH_POSITION, where);
                count = db.update(App.HierarchyItems.TABLE_NAME, values, finalWhere, whereArgs);
                reindexType = IndexingService.EntityType.HIERARCHY;
                uuid = values.getAsString(App.HierarchyItems.COLUMN_HIERARCHY_UUID);
                break;
            case FIELDWORKERS:
                count = db.update(App.FieldWorkers.TABLE_NAME, values, where,
                        whereArgs);
                break;
            case FIELDWORKER_ID:
                finalWhere = buildFinalWhere(uri,
                        App.FieldWorkers.ID_PATH_POSITION, where);
                count = db.update(App.FieldWorkers.TABLE_NAME, values,
                        finalWhere, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Context ctx = getContext();
        if (reindexType != null && uuid != null && isSearchEnabled(ctx)) {
            IndexingService.queueReindex(ctx, reindexType, uuid);
        }
        ctx.getContentResolver().notifyChange(uri, null);

        return count;
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + App.Individuals.TABLE_NAME + " ("
                    + App.Individuals._ID + " INTEGER,"
                    + App.Individuals.COLUMN_INDIVIDUAL_UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + App.Individuals.COLUMN_INDIVIDUAL_DOB + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_EXTID + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_GENDER + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_STATUS + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_NATIONALITY + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_OTHER_ID + " TEXT,"
                    + App.Individuals.COLUMN_INDIVIDUAL_ATTRS + " TEXT);");

            db.execSQL("CREATE INDEX INDIVIDUAL_UUID_INDEX ON " + App.Individuals.TABLE_NAME + "("
                    + App.Individuals.COLUMN_INDIVIDUAL_UUID + ") ; ");

            db.execSQL("CREATE INDEX INDIVIDUAL_EXTID_INDEX ON " + App.Individuals.TABLE_NAME + "("
                    + App.Individuals.COLUMN_INDIVIDUAL_EXTID + ") ; ");

            db.execSQL("CREATE INDEX INDIVIDUAL_RESIDENCY_INDEX ON " + App.Individuals.TABLE_NAME + "("
                    + App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID + ") ; ");


            db.execSQL("CREATE TABLE " + App.Locations.TABLE_NAME + " ("
                    + App.Locations._ID + " INTEGER,"
                    + App.Locations.COLUMN_LOCATION_EXTID + " TEXT NOT NULL,"
                    + App.Locations.COLUMN_LOCATION_UUID + " TEXT NOT NULL PRIMARY KEY,"
                    + App.Locations.COLUMN_LOCATION_HIERARCHY_UUID + " TEXT NOT NULL,"
                    + App.Locations.COLUMN_LOCATION_LATITUDE + " TEXT,"
                    + App.Locations.COLUMN_LOCATION_LONGITUDE + " TEXT,"
                    + App.Locations.COLUMN_LOCATION_MAP_AREA_NAME + " TEXT,"
                    + App.Locations.COLUMN_LOCATION_SECTOR_NAME + " INT,"
                    + App.Locations.COLUMN_LOCATION_BUILDING_NUMBER + " INT,"
                    + App.Locations.COLUMN_LOCATION_DESCRIPTION + " TEXT,"
                    + App.Locations.COLUMN_LOCATION_NAME + " TEXT NOT NULL,"
                    + App.Locations.COLUMN_LOCATION_ATTRS + " TEXT);");

            db.execSQL("CREATE INDEX LOCATION_EXTID_INDEX ON " + App.Locations.TABLE_NAME + "("
                    + App.Locations.COLUMN_LOCATION_EXTID + ") ; ");

            db.execSQL("CREATE INDEX LOCATION_HIERARCHY_UUID_INDEX ON " + App.Locations.TABLE_NAME + "("
                    + App.Locations.COLUMN_LOCATION_HIERARCHY_UUID + ") ; ");

            db.execSQL("CREATE INDEX LOCATION_UUID_INDEX ON " + App.Locations.TABLE_NAME + "("
                    + App.Locations.COLUMN_LOCATION_UUID + ") ; ");


            db.execSQL("CREATE TABLE " + App.HierarchyItems.TABLE_NAME + " ("
                    + App.HierarchyItems._ID + " INTEGER,"
                    + App.HierarchyItems.COLUMN_HIERARCHY_UUID + " TEXT NOT NULL PRIMARY KEY,"
                    + App.HierarchyItems.COLUMN_HIERARCHY_EXTID + " TEXT NOT NULL,"
                    + App.HierarchyItems.COLUMN_HIERARCHY_LEVEL + " TEXT NOT NULL,"
                    + App.HierarchyItems.COLUMN_HIERARCHY_NAME + " TEXT NOT NULL,"
                    + App.HierarchyItems.COLUMN_HIERARCHY_PARENT + " TEXT NOT NULL,"
                    + App.HierarchyItems.COLUMN_HIERARCHY_ATTRS + " TEXT);");

            db.execSQL("CREATE INDEX LOCATIONHIERARCHY_PARENT_INDEX ON " + App.HierarchyItems.TABLE_NAME + "("
                    + App.HierarchyItems.COLUMN_HIERARCHY_PARENT + ") ; ");

            db.execSQL("CREATE INDEX LOCATIONHIERARCHY_UUID_INDEX ON " + App.HierarchyItems.TABLE_NAME + "("
                    + App.HierarchyItems.COLUMN_HIERARCHY_UUID + ") ; ");

            db.execSQL("CREATE INDEX LOCATIONHIERARCHY_EXTID_INDEX ON " + App.HierarchyItems.TABLE_NAME + "("
                    + App.HierarchyItems.COLUMN_HIERARCHY_EXTID + ") ; ");


            db.execSQL("CREATE TABLE " + App.FieldWorkers.TABLE_NAME + " ("
                    + App.FieldWorkers._ID + " INTEGER,"
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_EXTID + " TEXT NOT NULL,"
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX + " TEXT NOT NULL,"
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME + " TEXT NOT NULL,"
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME + " TEXT NOT NULL,"
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD + " TEXT NOT NULL);");

            db.execSQL("CREATE INDEX FIELDWORKERS_EXTID_INDEX ON " + App.FieldWorkers.TABLE_NAME + "("
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_EXTID + ") ; ");

            db.execSQL("CREATE INDEX FIELDWORKERS_UUID_INDEX ON " + App.FieldWorkers.TABLE_NAME + "("
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_UUID + ") ; ");

            db.execSQL("CREATE INDEX FIELDWORKERS_ID_PREFIX_INDEX ON " + App.FieldWorkers.TABLE_NAME + "("
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX + ") ; ");

            db.execSQL("CREATE INDEX FIELDWORKERS_PASSWORD_INDEX ON " + App.FieldWorkers.TABLE_NAME + "("
                    + App.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD + ") ; ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 14) {
                throw new SQLiteException("Can't upgrade database from version " + oldVersion + " to " + newVersion);
            } else {
                Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            }
        }
    }
}
