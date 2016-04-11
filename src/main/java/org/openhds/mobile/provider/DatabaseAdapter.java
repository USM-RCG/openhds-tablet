package org.openhds.mobile.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.openhds.mobile.model.core.Supervisor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DatabaseAdapter {

	private static final String DATABASE_NAME = "entityData";
	private static final int DATABASE_VERSION = 16;

	private static final String SUPERVISOR_TABLE_NAME = "openhds_supervisor";
	private static final String KEY_ID = "_id";
	public static final String KEY_SUPERVISOR_NAME = "username";
	public static final String KEY_SUPERVISOR_PASS = "password";

	private static final String ASSOCIATION_TABLE_NAME = "path_to_forms";
	public static final String  KEY_PATH_ID = "path_id";
	public static final String  KEY_TO_FORM = "hierarchyPath";
	public static final String  KEY_FORM_PATH = "formPath";

	private static final String USER_DB_CREATE = "CREATE TABLE "
			+ SUPERVISOR_TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
			+ KEY_SUPERVISOR_NAME + " TEXT, " + KEY_SUPERVISOR_PASS + " TEXT)";

	private static final String ASSOCIATION_DB_CREATE = "CREATE TABLE "
			+ ASSOCIATION_TABLE_NAME + " (" + KEY_TO_FORM + " TEXT, " + KEY_FORM_PATH + " TEXT, CONSTRAINT "
			+ KEY_PATH_ID + " UNIQUE (" + KEY_TO_FORM + ", " +KEY_FORM_PATH +" ) )" ;

	private static DatabaseAdapter INSTANCE;

	public static synchronized DatabaseAdapter getInstance(Context ctx) {
		if (INSTANCE == null) {
			INSTANCE = new DatabaseAdapter(ctx);
		}
		return INSTANCE;
	}

	private DatabaseHelper dbHelper;

	protected DatabaseAdapter(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public Supervisor findSupervisorByUsername(String username) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
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
		SQLiteDatabase db = dbHelper.getWritableDatabase();
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
		SQLiteDatabase db = dbHelper.getWritableDatabase();
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

	public long createAssociation(String hierarchyPath, String filePath) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(KEY_TO_FORM, hierarchyPath);
			cv.put(KEY_FORM_PATH, filePath);
			long id = db.replaceOrThrow(ASSOCIATION_TABLE_NAME, null, cv);
			db.setTransactionSuccessful();
			return id;
		} finally {
			db.endTransaction();
		}
	}

	public Collection<String> findAssociatedPath(String hierarchyPath) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Set<String> formPaths = new HashSet<>();
		String[] columns = {KEY_FORM_PATH};
		String where = String.format("%s = ?", KEY_TO_FORM);
		String[] whereArgs = {hierarchyPath};
		Cursor cursor = db.query(ASSOCIATION_TABLE_NAME, columns, where, whereArgs, null, null, null);
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

	public Collection<String> findAllAssociatedPaths() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Set<String> associatedPaths = new HashSet<>();
		String[] columns = {KEY_FORM_PATH};
		Cursor cursor = db.query(ASSOCIATION_TABLE_NAME, columns, null, null, null, null, null);
		if (cursor != null) {
			try {
				while (cursor.moveToNext()) {
					associatedPaths.add(cursor.getString(cursor.getColumnIndex(KEY_FORM_PATH)));
				}
			} finally {
				cursor.close();
			}
		}
		return associatedPaths;
	}

	public void deleteAssociatedPath(String sentFilepath) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			String where = String.format("%s = ?", KEY_FORM_PATH);
			String[] whereArgs = {sentFilepath};
			db.delete(ASSOCIATION_TABLE_NAME, where, whereArgs);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(USER_DB_CREATE);
			db.execSQL(ASSOCIATION_DB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onCreate(db);
		}
	}
}
