package org.cimsbioko.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.cimsbioko.data.DataWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.cimsbioko.App.getApp;
import static org.cimsbioko.utilities.SQLUtils.makePlaceholders;

public class DatabaseAdapter {

    private static final String DATABASE_NAME = "entityData";
    private static final int DATABASE_VERSION = 19;

    private static final String FORM_PATH_TABLE_NAME = "path_to_forms";
    private static final String FORM_PATH_IDX_NAME = "path_id";
    private static final String KEY_HIER_PATH = "hierarchyPath";
    private static final String KEY_FORM_ID = "form_id";

    private static final String SYNC_HISTORY_TABLE_NAME = "sync_history";
    private static final String KEY_FINGERPRINT = "fingerprint";
    private static final String START_TIME_IDX_NAME = "start_time_idx";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_END_TIME = "end_time";
    private static final String KEY_RESULT = "result";

    private static final String FORM_PATH_CREATE = "CREATE TABLE IF NOT EXISTS " + FORM_PATH_TABLE_NAME + " ("
            + KEY_HIER_PATH + " TEXT, "
            + KEY_FORM_ID + " TEXT, CONSTRAINT " + FORM_PATH_IDX_NAME + " UNIQUE (" + KEY_FORM_ID + " ) )";

    private static final String SYNC_HISTORY_CREATE = "CREATE TABLE IF NOT EXISTS " + SYNC_HISTORY_TABLE_NAME + " ("
            + KEY_FINGERPRINT + " TEXT NOT NULL, "
            + KEY_START_TIME + " INTEGER NOT NULL, "
            + KEY_END_TIME + " INTEGER NOT NULL, "
            + KEY_RESULT + " TEXT NOT NULL)";

    private static final String START_TIME_IDX_CREATE = "CREATE INDEX IF NOT EXISTS " + START_TIME_IDX_NAME + " ON "
            + SYNC_HISTORY_TABLE_NAME + "(" + KEY_START_TIME + ")";

    private static final String FAVORITE_TABLE_NAME = "favorite";
    private static final String FAVORITE_CREATE = "CREATE TABLE IF NOT EXISTS " + FAVORITE_TABLE_NAME + " ("
            + KEY_HIER_PATH + " TEXT PRIMARY KEY)";

    private static DatabaseAdapter instance;

    public static synchronized DatabaseAdapter getInstance() {
        if (instance == null) {
            instance = new DatabaseAdapter();
        }
        return instance;
    }

    private final DatabaseHelper helper;

    protected DatabaseAdapter() {
        helper = new DatabaseHelper(getApp().getApplicationContext());
    }

    public long attachFormToHierarchy(String hierarchyPath, Long formId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        String currentAttachment = findHierarchyForForm(formId);
        if (currentAttachment != null) {
            detachFromHierarchy(singletonList(formId));
        }
        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_HIER_PATH, hierarchyPath);
            cv.put(KEY_FORM_ID, formId.toString());
            long id = db.replaceOrThrow(FORM_PATH_TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public String findHierarchyForForm(Long id) {
        if (id != null) {
            SQLiteDatabase db = helper.getReadableDatabase();
            String[] columns = {KEY_HIER_PATH};
            String where = String.format("%s = ?", KEY_FORM_ID);
            String[] whereArgs = {id.toString()};
            Cursor cursor = db.query(FORM_PATH_TABLE_NAME, columns, where, whereArgs, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        return cursor.getString(cursor.getColumnIndex(KEY_HIER_PATH));
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return null;
    }

    public Collection<Long> findFormsForHierarchy(String hierarchyPath) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Set<Long> formIds = new HashSet<>();
        String[] columns = {KEY_FORM_ID};
        String where = String.format("%s = ?", KEY_HIER_PATH);
        String[] whereArgs = {hierarchyPath};
        Cursor cursor = db.query(FORM_PATH_TABLE_NAME, columns, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    formIds.add(Long.valueOf(cursor.getString(cursor.getColumnIndex(KEY_FORM_ID))));
                }
            } finally {
                cursor.close();
            }
        }
        return formIds;
    }

    public void detachFromHierarchy(List<Long> formIds) {
        if (!formIds.isEmpty()) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            try {
                String where = String.format("%s in (%s)", KEY_FORM_ID, makePlaceholders(formIds.size()));
                String[] idStrings = new String[formIds.size()];
                for (int i=0; i<formIds.size(); i++) {
                    Long id = formIds.get(i);
                    idStrings[i] = id == null? null : id.toString();
                }
                db.delete(FORM_PATH_TABLE_NAME, where, idStrings);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public void addSyncResult(String fingerprint, long startTime, long endTime, String result) {
        final int MILLIS_IN_SEC = 1000;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_FINGERPRINT, fingerprint);
            cv.put(KEY_START_TIME, startTime / MILLIS_IN_SEC);
            cv.put(KEY_END_TIME, endTime / MILLIS_IN_SEC);
            cv.put(KEY_RESULT, result);
            db.insert(SYNC_HISTORY_TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void pruneSyncResults(int daysToKeep) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String where = String.format("%s > date('now','-%s days')", KEY_START_TIME, daysToKeep);
            db.delete(SYNC_HISTORY_TABLE_NAME, where, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Number[] getSyncResults() {
        ArrayList<Number> results = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String [] columns = {KEY_START_TIME, "(end_time-start_time)/60.0"};
        String where = String.format("%s = 'success'", KEY_RESULT);
        Cursor cursor = db.query(SYNC_HISTORY_TABLE_NAME, columns, where, null, null, null, KEY_START_TIME);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    results.add(cursor.getFloat(0)); // start time (seconds since unix epoch)
                    results.add(cursor.getFloat(1)); // sync duration (minutes)
                }
            } finally {
                cursor.close();
            }
        }
        return results.toArray(new Number[]{});
    }

    public long addFavorite(DataWrapper item) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_HIER_PATH, item.getHierarchyId());
            long id = db.insert(FAVORITE_TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public long removeFavorite(DataWrapper item) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String where = String.format("%s = ?", KEY_HIER_PATH);
            String [] whereArgs = {item.getHierarchyId()};
            long id = db.delete(FAVORITE_TABLE_NAME, where, whereArgs);
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public long removeFavorite(String hierarchyId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String where = String.format("%s = ?", KEY_HIER_PATH);
            String[] whereArgs = {hierarchyId};
            long id = db.delete(FAVORITE_TABLE_NAME, where, whereArgs);
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public List<String> getFavoriteIds() {
        ArrayList<String> results = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columns = {KEY_HIER_PATH};
        Cursor cursor = db.query(FAVORITE_TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    results.add(cursor.getString(0));
                }
            } finally {
                cursor.close();
            }
        }
        return results;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private final String TAG = DatabaseHelper.class.getSimpleName();

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private void execSQL(SQLiteDatabase db, String... statements) {
            for (String statement : statements) {
                db.execSQL(statement);
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "creating database");
            execSQL(db, FORM_PATH_CREATE, SYNC_HISTORY_CREATE, START_TIME_IDX_CREATE, FAVORITE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, String.format("upgrading db version %s to %s", oldVersion, newVersion));
            if (oldVersion < 17) {
                execSQL(db, SYNC_HISTORY_CREATE, START_TIME_IDX_CREATE);
            }
            if (oldVersion < 18) {
                execSQL(db, FAVORITE_CREATE);
            }
            if (oldVersion < 19) {
                execSQL(db, "DROP TABLE IF EXISTS " + FORM_PATH_TABLE_NAME);
                execSQL(db, FORM_PATH_CREATE);
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, String.format("downgrading db version %s to %s", oldVersion, newVersion));
            if (newVersion < 19) {
                execSQL(db, "DROP TABLE IF EXISTS " + FORM_PATH_TABLE_NAME);
            }
            if (newVersion < 18) {
                db.execSQL("DROP TABLE IF EXISTS " + FAVORITE_TABLE_NAME);
            }
            if (newVersion < 17) {
                db.execSQL("DROP TABLE IF EXISTS " + SYNC_HISTORY_TABLE_NAME);
            }
        }
    }
}
