package org.openhds.mobile.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.openhds.mobile.model.core.Supervisor;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openhds.mobile.utilities.SQLUtils.makePlaceholders;

public class DatabaseAdapter {

    private static final String DATABASE_NAME = "entityData";
    private static final int DATABASE_VERSION = 17;

    private static final String SUPERVISOR_TABLE_NAME = "openhds_supervisor";
    private static final String KEY_ID = "_id";
    private static final String KEY_SUPERVISOR_NAME = "username";
    private static final String KEY_SUPERVISOR_PASS = "password";

    private static final String FORM_PATH_TABLE_NAME = "path_to_forms";
    private static final String FORM_PATH_IDX_NAME = "path_id";
    private static final String KEY_HIER_PATH = "hierarchyPath";
    private static final String KEY_FORM_PATH = "formPath";

    private static final String SYNC_HISTORY_TABLE_NAME = "sync_history";
    private static final String KEY_FINGERPRINT = "fingerprint";
    private static final String START_TIME_IDX_NAME = "start_time_idx";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_END_TIME = "end_time";
    private static final String KEY_RESULT = "result";

    private static final String USER_CREATE = "CREATE TABLE IF NOT EXISTS " + SUPERVISOR_TABLE_NAME + " ("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_SUPERVISOR_NAME + " TEXT, "
            + KEY_SUPERVISOR_PASS + " TEXT)";

    private static final String FORM_PATH_CREATE = "CREATE TABLE IF NOT EXISTS " + FORM_PATH_TABLE_NAME + " ("
            + KEY_HIER_PATH + " TEXT, "
            + KEY_FORM_PATH + " TEXT, CONSTRAINT "
            + FORM_PATH_IDX_NAME + " UNIQUE (" + KEY_HIER_PATH + ", " + KEY_FORM_PATH + " ) )";

    private static final String SYNC_HISTORY_CREATE = "CREATE TABLE IF NOT EXISTS " + SYNC_HISTORY_TABLE_NAME + " ("
            + KEY_FINGERPRINT + " TEXT NOT NULL, "
            + KEY_START_TIME + " INTEGER NOT NULL, "
            + KEY_END_TIME + " INTEGER NOT NULL, "
            + KEY_RESULT + " TEXT NOT NULL)";

    private static final String START_TIME_IDX_CREATE = "CREATE INDEX IF NOT EXISTS " + START_TIME_IDX_NAME + " ON "
            + SYNC_HISTORY_TABLE_NAME + "(" + KEY_START_TIME + " DESC)";

    private static DatabaseAdapter instance;

    public static synchronized DatabaseAdapter getInstance(Context ctx) {
        if (instance == null) {
            instance = new DatabaseAdapter(ctx);
        }
        return instance;
    }

    private DatabaseHelper helper;

    protected DatabaseAdapter(Context context) {
        helper = new DatabaseHelper(context);
    }

    public Supervisor findSupervisorByUsername(String username) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columns = {KEY_ID, KEY_SUPERVISOR_NAME, KEY_SUPERVISOR_PASS};
        String where = String.format("%s = ?", KEY_SUPERVISOR_NAME);
        String[] whereArgs = {username};
        Cursor cursor = db.query(SUPERVISOR_TABLE_NAME, columns, where, whereArgs, null, null, null);
        Supervisor user = null;
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    user = new Supervisor();
                    user.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                    user.setName(cursor.getString(cursor.getColumnIndex(KEY_SUPERVISOR_NAME)));
                    user.setPassword(cursor.getString(cursor.getColumnIndex(KEY_SUPERVISOR_PASS)));
                }
            } finally {
                cursor.close();
            }
        }
        return user;
    }

    public long addSupervisor(Supervisor u) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_SUPERVISOR_NAME, u.getName());
            cv.put(KEY_SUPERVISOR_PASS, u.getPassword());
            long id = db.insert(SUPERVISOR_TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public int deleteSupervisor(Supervisor u) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int rowCount = -1;
        db.beginTransaction();
        try {
            String where = String.format("%s = ?", KEY_SUPERVISOR_NAME);
            String[] whereArgs = {u.getName()};
            rowCount = db.delete(SUPERVISOR_TABLE_NAME, where, whereArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return rowCount;
    }

    public long attachFormToHierarchy(String hierarchyPath, String formPath) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_HIER_PATH, hierarchyPath);
            cv.put(KEY_FORM_PATH, formPath);
            long id = db.replaceOrThrow(FORM_PATH_TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Updates all attachment records referencing an old filesystem path to a new filesystem path.
     *
     * @param oldPath the old filesystem path
     * @param newPath the new filesystem path
     * @return the number of records updated
     */
    public long updateAttachedPath(String oldPath, String newPath) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_FORM_PATH, newPath);
            final String where = String.format("%s = ?", KEY_FORM_PATH);
            final String[] whereArgs = {oldPath};
            long updateCount = db.update(FORM_PATH_TABLE_NAME, cv, where, whereArgs);
            db.setTransactionSuccessful();
            return updateCount;
        } finally {
            db.endTransaction();
        }
    }

    public String findHierarchyForForm(String filePath) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Set<String> formPaths = new HashSet<>();
        String[] columns = {KEY_HIER_PATH};
        String where = String.format("%s = ?", KEY_FORM_PATH);
        String[] whereArgs = {filePath};
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
        return null;
    }

    public Collection<String> findFormsForHierarchy(String hierarchyPath) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Set<String> formPaths = new HashSet<>();
        String[] columns = {KEY_FORM_PATH};
        String where = String.format("%s = ?", KEY_HIER_PATH);
        String[] whereArgs = {hierarchyPath};
        Cursor cursor = db.query(FORM_PATH_TABLE_NAME, columns, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    formPaths.add(cursor.getString(cursor.getColumnIndex(KEY_FORM_PATH)));
                }
            } finally {
                cursor.close();
            }
        }
        return formPaths;
    }

    public void detachFromHierarchy(List<String> formPaths) {
        if (!formPaths.isEmpty()) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            try {
                String where = String.format("%s in (%s)", KEY_FORM_PATH, makePlaceholders(formPaths.size()));
                String[] whereArgs = formPaths.toArray(new String[formPaths.size()]);
                db.delete(FORM_PATH_TABLE_NAME, where, whereArgs);
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
            execSQL(db, USER_CREATE, FORM_PATH_CREATE, SYNC_HISTORY_CREATE, START_TIME_IDX_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, String.format("upgrading db version %s to %s", oldVersion, newVersion));
            if (oldVersion < 17) {
                execSQL(db, SYNC_HISTORY_CREATE, START_TIME_IDX_CREATE);
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, String.format("downgrading db version %s to %s", oldVersion, newVersion));
            if (newVersion < 17) {
                db.execSQL("DROP TABLE IF EXISTS " + SYNC_HISTORY_TABLE_NAME);
            }
        }
    }
}
