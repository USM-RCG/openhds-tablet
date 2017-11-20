package org.openhds.mobile.provider;

import java.util.HashMap;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.search.IndexingService;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import static org.openhds.mobile.search.Utils.isSearchEnabled;

/**
 * ContentProvider for OpenHDS <br />
 * This class is based on the NotPadProvider sample in the Android SDK
 */
public class OpenHDSProvider extends ContentProvider {

    public static final String DATABASE_NAME = "openhds.db";
    public static final int DATABASE_VERSION = 13;

    private static final String TAG = "OpenHDSProvider";

    private static HashMap<String, String> individualsProjectionMap;
    private static HashMap<String, String> locationsProjectionMap;
    private static HashMap<String, String> hierarchyitemsProjectionMap;
    private static HashMap<String, String> relationshipsProjectionMap;
    private static HashMap<String, String> fieldworkersProjectionMap;
    private static HashMap<String, String> socialgroupsProjectionMap;
    private static HashMap<String, String> membershipsProjectionMap;

    private static final int INDIVIDUALS = 1;
    private static final int INDIVIDUAL_ID = 2;
    private static final int LOCATIONS = 3;
    private static final int LOCATION_ID = 4;
    private static final int HIERARCHYITEMS = 5;
    private static final int HIERARCHYITEM_ID = 6;
    private static final int RELATIONSHIPS = 11;
    private static final int RELATIONSHIP_ID = 12;
    private static final int FIELDWORKERS = 13;
    private static final int FIELDWORKER_ID = 14;
    private static final int SOCIALGROUPS = 15;
    private static final int SOCIALGROUP_ID = 16;
    private static final int MEMBERSHIPS = 17;
    private static final int MEMBERSHIPS_ID = 18;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "individuals", INDIVIDUALS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "individuals/#", INDIVIDUAL_ID);

        sUriMatcher.addURI(OpenHDS.AUTHORITY, "locations", LOCATIONS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "locations/#", LOCATION_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "hierarchyitems", HIERARCHYITEMS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "hierarchyitems/#", HIERARCHYITEM_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "relationships", RELATIONSHIPS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "relationships/#", RELATIONSHIP_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "fieldworkers", FIELDWORKERS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "fieldworkers/#", FIELDWORKER_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "socialgroups", SOCIALGROUPS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "socialgroups/#", SOCIALGROUP_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "memberships", MEMBERSHIPS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "memberships/#", MEMBERSHIPS_ID);


        individualsProjectionMap = new HashMap<>();

        // general individual columns
        individualsProjectionMap.put(OpenHDS.Individuals._ID, OpenHDS.Individuals._ID);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID, OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB, OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER, OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER, OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER, OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID, OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE, OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FULL_NAME, OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME + " || ' ' || " + OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME + " as " + OpenHDS.Individuals.COLUMN_INDIVIDUAL_FULL_NAME);

        // extensions for bioko project
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES, OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE, OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE_UNITS, OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE_UNITS);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER, OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME, OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER, OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE, OpenHDS.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_STATUS, OpenHDS.Individuals.COLUMN_INDIVIDUAL_STATUS);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_NATIONALITY, OpenHDS.Individuals.COLUMN_INDIVIDUAL_NATIONALITY);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_ID, OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_ID);

        locationsProjectionMap = new HashMap<>();
        locationsProjectionMap.put(OpenHDS.Locations._ID, OpenHDS.Locations._ID);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_EXTID, OpenHDS.Locations.COLUMN_LOCATION_EXTID);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_UUID, OpenHDS.Locations.COLUMN_LOCATION_UUID);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_UUID, OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_UUID);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_EXTID, OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_EXTID);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE, OpenHDS.Locations.COLUMN_LOCATION_LATITUDE);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE, OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_NAME, OpenHDS.Locations.COLUMN_LOCATION_NAME);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME, OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_CODE, OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_CODE);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME, OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME, OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME, OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER, OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_FLOOR_NUMBER, OpenHDS.Locations.COLUMN_LOCATION_FLOOR_NUMBER);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_REGION_NAME, OpenHDS.Locations.COLUMN_LOCATION_REGION_NAME);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_PROVINCE_NAME, OpenHDS.Locations.COLUMN_LOCATION_PROVINCE_NAME);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_SUB_DISTRICT_NAME, OpenHDS.Locations.COLUMN_LOCATION_SUB_DISTRICT_NAME);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_DISTRICT_NAME, OpenHDS.Locations.COLUMN_LOCATION_DISTRICT_NAME);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_HAS_RECIEVED_BEDNETS, OpenHDS.Locations.COLUMN_LOCATION_HAS_RECIEVED_BEDNETS);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_SPRAYING_EVALUATION, OpenHDS.Locations.COLUMN_LOCATION_SPRAYING_EVALUATION);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_DESCRIPTION, OpenHDS.Locations.COLUMN_LOCATION_DESCRIPTION);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_EVALUATION_STATUS, OpenHDS.Locations.COLUMN_LOCATION_EVALUATION_STATUS);

        hierarchyitemsProjectionMap = new HashMap<>();
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems._ID, OpenHDS.HierarchyItems._ID);
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID);
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID);
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL);
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME);
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT);

        relationshipsProjectionMap = new HashMap<>();
        relationshipsProjectionMap.put(OpenHDS.Relationships._ID, OpenHDS.Relationships._ID);
        relationshipsProjectionMap.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_UUID, OpenHDS.Relationships.COLUMN_RELATIONSHIP_UUID);
        relationshipsProjectionMap.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A, OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A);
        relationshipsProjectionMap.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B, OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B);
        relationshipsProjectionMap.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_TYPE, OpenHDS.Relationships.COLUMN_RELATIONSHIP_TYPE);
        relationshipsProjectionMap.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE, OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE);

        fieldworkersProjectionMap = new HashMap<>();
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers._ID, OpenHDS.FieldWorkers._ID);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID, OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_UUID, OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_UUID);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX, OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME, OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME, OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD, OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD);

        socialgroupsProjectionMap = new HashMap<>();
        socialgroupsProjectionMap.put(OpenHDS.SocialGroups._ID, OpenHDS.SocialGroups._ID);
        socialgroupsProjectionMap.put(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_UUID, OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_UUID);
        socialgroupsProjectionMap.put(OpenHDS.SocialGroups.COLUMN_LOCATION_UUID, OpenHDS.SocialGroups.COLUMN_LOCATION_UUID);
        socialgroupsProjectionMap.put(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_UUID, OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_UUID);
        socialgroupsProjectionMap.put(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_NAME, OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_NAME);

        membershipsProjectionMap = new HashMap<>();
        membershipsProjectionMap.put(OpenHDS.Memberships._ID, OpenHDS.Memberships._ID);
        membershipsProjectionMap.put(OpenHDS.Memberships.COLUMN_INDIVIDUAL_UUID, OpenHDS.Memberships.COLUMN_INDIVIDUAL_UUID);
        membershipsProjectionMap.put(OpenHDS.Memberships.COLUMN_MEMBERSHIP_UUID, OpenHDS.Memberships.COLUMN_MEMBERSHIP_UUID);
        membershipsProjectionMap.put(OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_UUID, OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_UUID);
        membershipsProjectionMap.put(OpenHDS.Memberships.COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD, OpenHDS.Memberships.COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD);
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
        int inserted = -1;
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
                qb.setTables(OpenHDS.Individuals.TABLE_NAME);
                qb.setProjectionMap(individualsProjectionMap);
                break;
            case INDIVIDUAL_ID:
                qb.setTables(OpenHDS.Individuals.TABLE_NAME);
                qb.setProjectionMap(individualsProjectionMap);
                qb.appendWhere(OpenHDS.Individuals._ID
                        + "="
                        + uri.getPathSegments().get(
                        OpenHDS.Individuals.NOTE_ID_PATH_POSITION));
                break;
            case LOCATIONS:
                qb.setTables(OpenHDS.Locations.TABLE_NAME);
                qb.setProjectionMap(locationsProjectionMap);
                break;
            case LOCATION_ID:
                qb.setTables(OpenHDS.Locations.TABLE_NAME);
                qb.setProjectionMap(locationsProjectionMap);
                qb.appendWhere(OpenHDS.Locations._ID
                        + "="
                        + uri.getPathSegments().get(
                        OpenHDS.Locations.NOTE_ID_PATH_POSITION));
                break;
            case HIERARCHYITEMS:
                qb.setTables(OpenHDS.HierarchyItems.TABLE_NAME);
                qb.setProjectionMap(hierarchyitemsProjectionMap);
                break;
            case HIERARCHYITEM_ID:
                qb.setTables(OpenHDS.HierarchyItems.TABLE_NAME);
                qb.setProjectionMap(hierarchyitemsProjectionMap);
                qb.appendWhere(OpenHDS.HierarchyItems._ID
                        + "="
                        + uri.getPathSegments().get(
                        OpenHDS.HierarchyItems.NOTE_ID_PATH_POSITION));

                break;
            case RELATIONSHIPS:
                qb.setTables(OpenHDS.Relationships.TABLE_NAME);
                qb.setProjectionMap(relationshipsProjectionMap);
                break;
            case RELATIONSHIP_ID:
                qb.setTables(OpenHDS.Relationships.TABLE_NAME);
                qb.setProjectionMap(relationshipsProjectionMap);
                qb.appendWhere(OpenHDS.Relationships._ID
                        + "="
                        + uri.getPathSegments().get(
                        OpenHDS.Relationships.ID_PATH_POSITION));
                break;
            case FIELDWORKERS:
                qb.setTables(OpenHDS.FieldWorkers.TABLE_NAME);
                qb.setProjectionMap(fieldworkersProjectionMap);
                break;
            case FIELDWORKER_ID:
                qb.setTables(OpenHDS.FieldWorkers.TABLE_NAME);
                qb.setProjectionMap(fieldworkersProjectionMap);
                qb.appendWhere(OpenHDS.FieldWorkers._ID
                        + "="
                        + uri.getPathSegments().get(
                        OpenHDS.FieldWorkers.ID_PATH_POSITION));
                break;
            case SOCIALGROUPS:
                qb.setTables(OpenHDS.SocialGroups.TABLE_NAME);
                qb.setProjectionMap(socialgroupsProjectionMap);
                break;
            case SOCIALGROUP_ID:
                qb.setTables(OpenHDS.SocialGroups.TABLE_NAME);
                qb.setProjectionMap(socialgroupsProjectionMap);
                qb.appendWhere(OpenHDS.SocialGroups._ID
                        + "="
                        + uri.getPathSegments().get(
                        OpenHDS.SocialGroups.ID_PATH_POSITION));
                break;
            case MEMBERSHIPS:
                qb.setTables(OpenHDS.Memberships.TABLE_NAME);
                qb.setProjectionMap(membershipsProjectionMap);
                break;
            case MEMBERSHIPS_ID:
                qb.setTables(OpenHDS.Memberships.TABLE_NAME);
                qb.setProjectionMap(membershipsProjectionMap);
                qb.appendWhere(OpenHDS.Memberships._ID
                        + "="
                        + uri.getPathSegments().get(
                        OpenHDS.Memberships.ID_PATH_POSITION));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = OpenHDS.DEFAULT_SORT_ORDER;
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
                return OpenHDS.Individuals.CONTENT_TYPE;
            case INDIVIDUAL_ID:
                return OpenHDS.Individuals.CONTENT_ITEM_TYPE;
            case LOCATIONS:
                return OpenHDS.Locations.CONTENT_TYPE;
            case LOCATION_ID:
                return OpenHDS.Locations.CONTENT_ITEM_TYPE;
            case HIERARCHYITEMS:
                return OpenHDS.HierarchyItems.CONTENT_TYPE;
            case HIERARCHYITEM_ID:
                return OpenHDS.HierarchyItems.CONTENT_ITEM_TYPE;
            case RELATIONSHIPS:
                return OpenHDS.Relationships.CONTENT_TYPE;
            case RELATIONSHIP_ID:
                return OpenHDS.Relationships.CONTENT_ITEM_TYPE;
            case FIELDWORKERS:
                return OpenHDS.FieldWorkers.CONTENT_TYPE;
            case FIELDWORKER_ID:
                return OpenHDS.FieldWorkers.CONTENT_ITEM_TYPE;
            case SOCIALGROUPS:
                return OpenHDS.SocialGroups.CONTENT_TYPE;
            case SOCIALGROUP_ID:
                return OpenHDS.SocialGroups.CONTENT_ITEM_TYPE;
            case MEMBERSHIPS:
                return OpenHDS.Memberships.CONTENT_TYPE;
            case MEMBERSHIPS_ID:
                return OpenHDS.Memberships.CONTENT_ITEM_TYPE;
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
                table = OpenHDS.Individuals.TABLE_NAME;
                contentUriBase = OpenHDS.Individuals.CONTENT_ID_URI_BASE;
                reindexType = IndexingService.EntityType.INDIVIDUAL;
                uuid = initialValues.getAsString(OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID);
                break;
            case LOCATIONS:
                table = OpenHDS.Locations.TABLE_NAME;
                contentUriBase = OpenHDS.Locations.CONTENT_ID_URI_BASE;
                reindexType = IndexingService.EntityType.LOCATION;
                uuid = initialValues.getAsString(OpenHDS.Locations.COLUMN_LOCATION_UUID);
                break;
            case HIERARCHYITEMS:
                table = OpenHDS.HierarchyItems.TABLE_NAME;
                contentUriBase = OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE;
                reindexType = IndexingService.EntityType.HIERARCHY;
                uuid = initialValues.getAsString(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID);
                break;
            case RELATIONSHIPS:
                table = OpenHDS.Relationships.TABLE_NAME;
                contentUriBase = OpenHDS.Relationships.CONTENT_ID_URI_BASE;
                break;
            case FIELDWORKERS:
                table = OpenHDS.FieldWorkers.TABLE_NAME;
                contentUriBase = OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE;
                break;
            case SOCIALGROUPS:
                table = OpenHDS.SocialGroups.TABLE_NAME;
                contentUriBase = OpenHDS.SocialGroups.CONTENT_ID_URI_BASE;
                break;
            case MEMBERSHIPS:
                table = OpenHDS.Memberships.TABLE_NAME;
                contentUriBase = OpenHDS.Memberships.CONTENT_ID_URI_BASE;
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
                count = db.delete(OpenHDS.Individuals.TABLE_NAME, where, whereArgs);
                break;
            case INDIVIDUAL_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.Individuals.NOTE_ID_PATH_POSITION, where);
                count = db.delete(OpenHDS.Individuals.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            case LOCATIONS:
                count = db.delete(OpenHDS.Locations.TABLE_NAME, where, whereArgs);
                break;
            case LOCATION_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.Locations.NOTE_ID_PATH_POSITION, where);
                count = db.delete(OpenHDS.Locations.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            case HIERARCHYITEMS:
                count = db.delete(OpenHDS.HierarchyItems.TABLE_NAME, where,
                        whereArgs);
                break;
            case HIERARCHYITEM_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.HierarchyItems.NOTE_ID_PATH_POSITION, where);
                count = db.delete(OpenHDS.HierarchyItems.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            case RELATIONSHIPS:
                count = db.delete(OpenHDS.Relationships.TABLE_NAME, where,
                        whereArgs);
                break;
            case RELATIONSHIP_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.Relationships.ID_PATH_POSITION, where);
                count = db.delete(OpenHDS.Relationships.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            case FIELDWORKERS:
                count = db
                        .delete(OpenHDS.FieldWorkers.TABLE_NAME, where, whereArgs);
                break;
            case FIELDWORKER_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.FieldWorkers.ID_PATH_POSITION, where);
                count = db.delete(OpenHDS.FieldWorkers.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            case SOCIALGROUPS:
                count = db
                        .delete(OpenHDS.SocialGroups.TABLE_NAME, where, whereArgs);
                break;
            case SOCIALGROUP_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.SocialGroups.ID_PATH_POSITION, where);
                count = db.delete(OpenHDS.SocialGroups.TABLE_NAME, finalWhere,
                        whereArgs);
                break;
            case MEMBERSHIPS:
                count = db.delete(OpenHDS.Memberships.TABLE_NAME, where, whereArgs);
                break;
            case MEMBERSHIPS_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.Memberships.ID_PATH_POSITION, where);
                count = db.delete(OpenHDS.Memberships.TABLE_NAME, finalWhere,
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
                count = db.update(OpenHDS.Individuals.TABLE_NAME, values, where, whereArgs);
                reindexType = IndexingService.EntityType.INDIVIDUAL;
                uuid = values.getAsString(OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID);
                break;
            case INDIVIDUAL_ID:
                finalWhere = buildFinalWhere(uri, OpenHDS.Individuals.NOTE_ID_PATH_POSITION, where);
                count = db.update(OpenHDS.Individuals.TABLE_NAME, values, finalWhere, whereArgs);
                reindexType = IndexingService.EntityType.INDIVIDUAL;
                uuid = values.getAsString(OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID);
                break;
            case LOCATIONS:
                count = db.update(OpenHDS.Locations.TABLE_NAME, values, where, whereArgs);
                reindexType = IndexingService.EntityType.LOCATION;
                uuid = values.getAsString(OpenHDS.Locations.COLUMN_LOCATION_UUID);
                break;
            case LOCATION_ID:
                finalWhere = buildFinalWhere(uri, OpenHDS.Locations.NOTE_ID_PATH_POSITION, where);
                count = db.update(OpenHDS.Locations.TABLE_NAME, values, finalWhere, whereArgs);
                reindexType = IndexingService.EntityType.LOCATION;
                uuid = values.getAsString(OpenHDS.Locations.COLUMN_LOCATION_UUID);
                break;
            case HIERARCHYITEMS:
                count = db.update(OpenHDS.HierarchyItems.TABLE_NAME, values, where, whereArgs);
                reindexType = IndexingService.EntityType.HIERARCHY;
                uuid = values.getAsString(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID);
                break;
            case HIERARCHYITEM_ID:
                finalWhere = buildFinalWhere(uri, OpenHDS.HierarchyItems.NOTE_ID_PATH_POSITION, where);
                count = db.update(OpenHDS.HierarchyItems.TABLE_NAME, values, finalWhere, whereArgs);
                reindexType = IndexingService.EntityType.HIERARCHY;
                uuid = values.getAsString(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID);
                break;
            case RELATIONSHIPS:
                count = db.update(OpenHDS.Relationships.TABLE_NAME, values, where,
                        whereArgs);
                break;
            case RELATIONSHIP_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.Relationships.ID_PATH_POSITION, where);
                count = db.update(OpenHDS.Relationships.TABLE_NAME, values,
                        finalWhere, whereArgs);
                break;
            case FIELDWORKERS:
                count = db.update(OpenHDS.FieldWorkers.TABLE_NAME, values, where,
                        whereArgs);
                break;
            case FIELDWORKER_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.FieldWorkers.ID_PATH_POSITION, where);
                count = db.update(OpenHDS.FieldWorkers.TABLE_NAME, values,
                        finalWhere, whereArgs);
                break;
            case SOCIALGROUPS:
                count = db.update(OpenHDS.SocialGroups.TABLE_NAME, values, where,
                        whereArgs);
                break;
            case SOCIALGROUP_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.SocialGroups.ID_PATH_POSITION, where);
                count = db.update(OpenHDS.SocialGroups.TABLE_NAME, values,
                        finalWhere, whereArgs);
                break;
            case MEMBERSHIPS:
                count = db.update(OpenHDS.Memberships.TABLE_NAME, values, where,
                        whereArgs);
                break;
            case MEMBERSHIPS_ID:
                finalWhere = buildFinalWhere(uri,
                        OpenHDS.Memberships.ID_PATH_POSITION, where);
                count = db.update(OpenHDS.Memberships.TABLE_NAME, values,
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
            db.execSQL("CREATE TABLE "
                    + OpenHDS.Individuals.TABLE_NAME
                    + " ("
                    + OpenHDS.Individuals._ID
                    + " INTEGER,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID
                    + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE_UNITS
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_STATUS
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_NATIONALITY
                    + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_ID
                    + " TEXT);");

            db.execSQL("CREATE INDEX INDIVIDUAL_UUID_INDEX ON " + OpenHDS.Individuals.TABLE_NAME +
                    "("+ OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID + ") ; ");

            db.execSQL("CREATE INDEX INDIVIDUAL_EXTID_INDEX ON " + OpenHDS.Individuals.TABLE_NAME +
                    "("+ OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + ") ; ");

            db.execSQL("CREATE INDEX INDIVIDUAL_RESIDENCY_INDEX ON " + OpenHDS.Individuals.TABLE_NAME +
                    "("+ OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID + ") ; ");


            db.execSQL("CREATE TABLE " + OpenHDS.Locations.TABLE_NAME + " ("
                    + OpenHDS.Locations._ID + " INTEGER,"
                    + OpenHDS.Locations.COLUMN_LOCATION_EXTID
                    + " TEXT NOT NULL,"
                    + OpenHDS.Locations.COLUMN_LOCATION_UUID
                    + " TEXT NOT NULL PRIMARY KEY,"
                    + OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_UUID
                    + " TEXT NOT NULL,"
                    + OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_EXTID + " TEXT,"
                    + OpenHDS.Locations.COLUMN_LOCATION_LATITUDE + " TEXT,"
                    + OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE + " TEXT,"
                    + OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME
                    + " TEXT,"
                    + OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_CODE
                    + " TEXT,"
                    + OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME
                    + " TEXT,"
                    + OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME
                    + " INT," + OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER
                    + " INT," + OpenHDS.Locations.COLUMN_LOCATION_FLOOR_NUMBER
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_REGION_NAME
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_PROVINCE_NAME
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_SUB_DISTRICT_NAME
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_DISTRICT_NAME
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_HAS_RECIEVED_BEDNETS
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_SPRAYING_EVALUATION
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_DESCRIPTION
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_EVALUATION_STATUS
                    + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_NAME
                    + " TEXT NOT NULL);");

            db.execSQL("CREATE INDEX LOCATION_EXTID_INDEX ON " + OpenHDS.Locations.TABLE_NAME +
                    "("+ OpenHDS.Locations.COLUMN_LOCATION_EXTID + ") ; ");

            db.execSQL("CREATE INDEX LOCATION_HIERARCHY_UUID_INDEX ON " + OpenHDS.Locations.TABLE_NAME +
                    "("+ OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_UUID + ") ; ");

            db.execSQL("CREATE INDEX LOCATION_UUID_INDEX ON " + OpenHDS.Locations.TABLE_NAME +
                    "("+ OpenHDS.Locations.COLUMN_LOCATION_UUID + ") ; ");


            db.execSQL("CREATE TABLE " + OpenHDS.HierarchyItems.TABLE_NAME
                    + " (" + OpenHDS.HierarchyItems._ID
                    + " INTEGER,"
                    + OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID
                    + " TEXT NOT NULL PRIMARY KEY,"
                    + OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID
                    + " TEXT NOT NULL,"
                    + OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL
                    + " TEXT NOT NULL,"
                    + OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME
                    + " TEXT NOT NULL,"
                    + OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT
                    + " TEXT NOT NULL);");

            db.execSQL("CREATE INDEX LOCATIONHIERARCHY_PARENT_INDEX ON " + OpenHDS.HierarchyItems.TABLE_NAME +
                    "("+ OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT + ") ; ");

            db.execSQL("CREATE INDEX LOCATIONHIERARCHY_UUID_INDEX ON " + OpenHDS.HierarchyItems.TABLE_NAME +
                    "("+ OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID + ") ; ");

            db.execSQL("CREATE INDEX LOCATIONHIERARCHY_EXTID_INDEX ON " + OpenHDS.HierarchyItems.TABLE_NAME +
                    "("+ OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID + ") ; ");

            db.execSQL("CREATE TABLE " + OpenHDS.Relationships.TABLE_NAME
                    + " (" + OpenHDS.Relationships._ID
                    + " INTEGER,"
                    + OpenHDS.Relationships.COLUMN_RELATIONSHIP_UUID
                    + " TEXT NOT NULL PRIMARY KEY,"
                    + OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A
                    + " TEXT NOT NULL,"
                    + OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B
                    + " TEXT NOT NULL,"
                    + OpenHDS.Relationships.COLUMN_RELATIONSHIP_TYPE
                    + " TEXT NOT NULL,"
                    + OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE
                    + " TEXT NOT NULL);");

            db.execSQL("CREATE INDEX RELATIONSHIP_INDIVIDUAL_A_INDEX ON " + OpenHDS.Relationships.TABLE_NAME +
                    "("+ OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A + ") ; ");

            db.execSQL("CREATE INDEX RELATIONSHIP_INDIVIDUAL_B_INDEX ON " + OpenHDS.Relationships.TABLE_NAME +
                    "("+ OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B + ") ; ");

            db.execSQL("CREATE INDEX RELATIONSHIP_UUID_INDEX ON " + OpenHDS.Relationships.TABLE_NAME +
                    "("+ OpenHDS.Relationships.COLUMN_RELATIONSHIP_UUID + ") ; ");


            db.execSQL("CREATE TABLE " + OpenHDS.FieldWorkers.TABLE_NAME + " ("
                    + OpenHDS.FieldWorkers._ID + " INTEGER,"
                    + OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_UUID
                    + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID
                    + " TEXT NOT NULL,"
                    + OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX
                    + " TEXT NOT NULL,"
                    + OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME
                    + " TEXT NOT NULL,"
                    + OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME
                    + " TEXT NOT NULL,"
                    + OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD
                    + " TEXT NOT NULL);");

            db.execSQL("CREATE INDEX FIELDWORKERS_EXTID_INDEX ON " + OpenHDS.FieldWorkers.TABLE_NAME +
                    "("+ OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID + ") ; ");

            db.execSQL("CREATE INDEX FIELDWORKERS_UUID_INDEX ON " + OpenHDS.FieldWorkers.TABLE_NAME +
                    "("+ OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_UUID + ") ; ");

            db.execSQL("CREATE INDEX FIELDWORKERS_ID_PREFIX_INDEX ON " + OpenHDS.FieldWorkers.TABLE_NAME +
                    "("+ OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX + ") ; ");

            db.execSQL("CREATE INDEX FIELDWORKERS_PASSWORD_INDEX ON " + OpenHDS.FieldWorkers.TABLE_NAME +
                    "("+ OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD + ") ; ");

            db.execSQL("CREATE TABLE "
                    + OpenHDS.SocialGroups.TABLE_NAME
                    + " ("
                    + OpenHDS.SocialGroups._ID
                    + " INTEGER,"
                    + OpenHDS.SocialGroups.COLUMN_LOCATION_UUID
                    + " TEXT NOT NULL,"
                    + OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_UUID
                    + " TEXT NOT NULL PRIMARY KEY,"
                    + OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_UUID
                    + " TEXT NOT NULL,"
                    + OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_NAME
                    + " TEXT NOT NULL);");

            db.execSQL("CREATE INDEX SOCIALGROUP_HEAD_INDEX ON " + OpenHDS.SocialGroups.TABLE_NAME +
                    "("+ OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_UUID + ") ; ");

            db.execSQL("CREATE INDEX SOCIALGROUP_LOCATION_INDEX ON " + OpenHDS.SocialGroups.TABLE_NAME +
                    "("+ OpenHDS.SocialGroups.COLUMN_LOCATION_UUID + ") ; ");

            db.execSQL("CREATE INDEX SOCIALGROUP_UUID_INDEX ON " + OpenHDS.SocialGroups.TABLE_NAME +
                    "("+ OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_UUID + ") ; ");

            db.execSQL("CREATE TABLE "
                    + OpenHDS.Memberships.TABLE_NAME
                    + " ("
                    + OpenHDS.Memberships._ID
                    + " INTEGER,"
                    + OpenHDS.Memberships.COLUMN_MEMBERSHIP_UUID
                    + " TEXT NOT NULL PRIMARY KEY,"
                    + OpenHDS.Memberships.COLUMN_INDIVIDUAL_UUID
                    + " TEXT NOT NULL,"
                    + OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_UUID
                    + " TEXT NOT NULL,"
                    + OpenHDS.Memberships.COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD
                    + " TEXT NOT NULL);");

            db.execSQL("CREATE INDEX MEMBERSHIP_INDIVIDUAL_INDEX ON " + OpenHDS.Memberships.TABLE_NAME +
                    "("+ OpenHDS.Memberships.COLUMN_INDIVIDUAL_UUID + ") ; ");

            db.execSQL("CREATE INDEX MEMBERSHIP_SOCIALGROUP_INDEX ON " + OpenHDS.Memberships.TABLE_NAME +
                    "("+ OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_UUID + ") ; ");

            db.execSQL("CREATE INDEX MEMBERSHIP_UUID_INDEX ON " + OpenHDS.Memberships.TABLE_NAME +
                    "("+ OpenHDS.Memberships.COLUMN_MEMBERSHIP_UUID + ") ; ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Memberships.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.SocialGroups.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.FieldWorkers.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Relationships.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Individuals.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.HierarchyItems.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Locations.TABLE_NAME);
            onCreate(db);
        }
    }
}
