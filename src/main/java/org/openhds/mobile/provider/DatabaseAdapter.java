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

	private DatabaseHelper dbHelper;
	private SQLiteDatabase database;

	public DatabaseAdapter(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public Supervisor findSupervisorByUsername(String username) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Supervisor user = null;
		try {
			Cursor c = db.query(SUPERVISOR_TABLE_NAME, new String[] { KEY_ID,
					KEY_SUPERVISOR_NAME, KEY_SUPERVISOR_PASS },
					KEY_SUPERVISOR_NAME + " = ?", new String[] { username },
					null, null, null);
			boolean found = c.moveToNext();
			if (!found) {
				c.close();
				return null;
			}

			user = new Supervisor();
			user.setId(c.getLong(c.getColumnIndex(KEY_ID)));
			user.setName(c.getString(c.getColumnIndex(KEY_SUPERVISOR_NAME)));
			user.setPassword(c.getString(c.getColumnIndex(KEY_SUPERVISOR_PASS)));
			c.close();
		} catch (Exception e) {
			Log.w("findUserByUsername", e.getMessage());
		} finally {
			db.close();
		}
		return user;
	}

	public long addSupervisor(Supervisor u) {
		long id = -1;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(KEY_SUPERVISOR_NAME, u.getName());
			cv.put(KEY_SUPERVISOR_PASS, u.getPassword());

			id = db.insert(SUPERVISOR_TABLE_NAME, null, cv);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		db.close();
		return id;
	}

	public int deleteSupervisor(Supervisor u) {
		int rowCount = -1;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			rowCount = db.delete(SUPERVISOR_TABLE_NAME, KEY_SUPERVISOR_NAME + " = ?", new String[]{u.getName()});
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		db.close();
		return rowCount;
	}

	public long createAssociation(String hierarchyPath, String filePath) {
		long id = -1;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(KEY_TO_FORM, hierarchyPath);
			cv.put(KEY_FORM_PATH, filePath);
			id = db.replaceOrThrow(ASSOCIATION_TABLE_NAME, null, cv);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
		return id;
	}

	public Collection<String> findAssociatedPath(String hierarchyPath) {
		Set<String> formPaths = new HashSet<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(ASSOCIATION_TABLE_NAME, new String[]{KEY_FORM_PATH},
					KEY_TO_FORM + "= ?", new String[]{hierarchyPath}, null, null, null);
			if (cursor == null) {
				return null;
			}
			while (cursor.moveToNext()) {
				String formPath;
				formPath = cursor.getString(cursor.getColumnIndex(KEY_FORM_PATH));
				formPaths.add(formPath);
			}
			cursor.close();
		} catch (Exception e) {
			Log.w("findUserByUsername", e.getMessage());
		}
		return formPaths;
	}

	public Collection<String> findAllAssociatedPaths() {
		Set<String> associatedPaths = new HashSet<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(ASSOCIATION_TABLE_NAME, new String[]{KEY_FORM_PATH}, null, null, null, null, null);
			if (cursor == null) {
				return null;
			}
			while (cursor.moveToNext()) {
				String formPath;
				formPath = cursor.getString(cursor.getColumnIndex(KEY_FORM_PATH));
				associatedPaths.add(formPath);
			}
			cursor.close();
		} catch (Exception e) {
			Log.w("findUserByUsername", e.getMessage());
		}
		return associatedPaths;
	}

	public void deleteAssociatedPath(String sentFilepath) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		db.delete(ASSOCIATION_TABLE_NAME, KEY_FORM_PATH + " = ?", new String[]{"" + sentFilepath});
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public SQLiteDatabase getDatabase() {
		return database;
	}

	public void setDatabase(SQLiteDatabase database) {
		this.database = database;
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
