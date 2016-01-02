package org.openhds.mobile.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import org.openhds.mobile.model.form.FormSubmissionRecord;
import org.openhds.mobile.model.core.Supervisor;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DatabaseAdapter {
	private static final String DATABASE_NAME = "entityData";
	private static final int DATABASE_VERSION = 16;

	private static final String FORM_TABLE_NAME = "formsubmission";
	private static final String KEY_ID = "_id";
	public static final String KEY_REMOTE_ID = "remote_id";
	public static final String KEY_FORMOWNER_ID = "form_owner_id";
	public static final String KEY_FORM_TYPE = "form_type";
	public static final String KEY_FORM_INSTANCE = "form_instance";
	public static final String KEY_FORM_DATETIME = "form_datetime";
	public static final String KEY_ODK_URI = "odk_uri";
	public static final String KEY_ODK_FORM_ID = "form_id";
	public static final String KEY_FORM_COMPLETED = "form_completed";
	public static final String KEY_REVIEW = "form_review";

	private static final String ERROR_TABLE_NAME = "formsubmission_msg";
	public static final String KEY_FORM_ID = "form_id";
	public static final String KEY_FORM_MSG = "message";

	private static final String SUPERVISOR_TABLE_NAME = "openhds_supervisor";
	public static final String KEY_SUPERVISOR_NAME = "username";
	public static final String KEY_SUPERVISOR_PASS = "password";

	private static final String ASSOCIATION_TABLE_NAME = "path_to_forms";
	public static final String  KEY_PATH_ID = "path_id";
	public static final String  KEY_TO_FORM = "hierarchyPath";
	public static final String  KEY_FORM_PATH = "formPath";

	private static final String FORM_DB_CREATE = "CREATE TABLE "
			+ FORM_TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
			+ KEY_FORMOWNER_ID + " TEXT, " + KEY_FORM_TYPE + " TEXT, "
			+ KEY_FORM_INSTANCE + " TEXT, " + KEY_FORM_DATETIME + " TEXT, "
			+ KEY_REMOTE_ID + " INTEGER, " + KEY_ODK_URI + " TEXT, "
			+ KEY_ODK_FORM_ID + " TEXT, " + KEY_FORM_COMPLETED
			+ " INTEGER DEFAULT 0, " + KEY_REVIEW + " INTEGER DEFAULT 0)";

	private static final String MESSAGE_DB_CREATE = "CREATE TABLE "
			+ ERROR_TABLE_NAME + " (" + KEY_FORM_ID + " INTEGER, "
			+ KEY_FORM_MSG + " TEXT)";

	private static final String USER_DB_CREATE = "CREATE TABLE "
			+ SUPERVISOR_TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
			+ KEY_SUPERVISOR_NAME + " TEXT, " + KEY_SUPERVISOR_PASS + " TEXT)";

	private static final String ASSOCIATION_DB_CREATE = "CREATE TABLE "
			+ ASSOCIATION_TABLE_NAME + " (" + KEY_TO_FORM + " TEXT, " + KEY_FORM_PATH + " TEXT, CONSTRAINT "
			+ KEY_PATH_ID + " UNIQUE (" + KEY_TO_FORM + ", " +KEY_FORM_PATH +" ) )" ;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd_HH_mm_ss_SSS");

	private DatabaseHelper dbHelper;
	private SQLiteDatabase database;

	public DatabaseAdapter(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public DatabaseAdapter open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
		database.close();
	}

	public void saveFormSubmission(FormSubmissionRecord fs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		long cnt = DatabaseUtils.longForQuery(db, "SELECT COUNT(_id) FROM "
				+ FORM_TABLE_NAME + " WHERE " + KEY_REMOTE_ID + " = ?",
				new String[] { fs.getRemoteId() + "" });
		if (cnt > 0) {
			db.close();
			return;
		}

		if (fs.getErrors().size() == 0) {
			fs.setNeedReview(true);
		}

		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(KEY_FORMOWNER_ID, fs.getFormOwnerId());
			cv.put(KEY_FORM_TYPE, fs.getFormType());
			cv.put(KEY_FORM_INSTANCE, fs.getPartialForm());
			cv.put(KEY_FORM_DATETIME, getCurrentDateTime());
			cv.put(KEY_ODK_FORM_ID, fs.getFormId());
			cv.put(KEY_REMOTE_ID, fs.getRemoteId());
			cv.put(KEY_REVIEW, fs.isNeedReview() ? 1 : 0);
			long rowId = db.insert(FORM_TABLE_NAME, null, cv);

			for (String error : fs.getErrors()) {
				cv = new ContentValues();
				cv.put(KEY_FORM_ID, rowId);
				cv.put(KEY_FORM_MSG, error);
				db.insert(ERROR_TABLE_NAME, null, cv);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		db.close();
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

	public long supervisorCount() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		long rows = DatabaseUtils.queryNumEntries(db, SUPERVISOR_TABLE_NAME);
		db.close();
		return rows;
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
			rowCount = db.delete(SUPERVISOR_TABLE_NAME, KEY_SUPERVISOR_NAME
					+ " = ?", new String[]{u.getName()});
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		db.close();
		return rowCount;
	}

	public Cursor getFormsForUsername(String user) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(FORM_TABLE_NAME, new String[] { KEY_ID,
				KEY_FORM_TYPE, KEY_FORMOWNER_ID, KEY_REVIEW }, KEY_FORMOWNER_ID
				+ " = ?", new String[] { user }, null, null, null);
	}

	public FormSubmissionRecord findSubmissionById(long id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(FORM_TABLE_NAME, null, KEY_ID + " = ?",
				new String[] { id + "" }, null, null, null);
		cursor.moveToNext();
		FormSubmissionRecord record = new FormSubmissionRecord();
		record.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
		record.setFormOwnerId(cursor.getString(cursor
				.getColumnIndex(KEY_FORMOWNER_ID)));
		record.setFormType(cursor.getString(cursor
				.getColumnIndex(KEY_FORM_TYPE)));
		record.setPartialForm(cursor.getString(cursor
				.getColumnIndex(KEY_FORM_INSTANCE)));
		record.setSaveDate(cursor.getString(cursor
				.getColumnIndex(KEY_FORM_DATETIME)));
		record.setOdkUri(cursor.getString(cursor.getColumnIndex(KEY_ODK_URI)));
		record.setFormId(cursor.getString(cursor
				.getColumnIndex(KEY_ODK_FORM_ID)));
		record.setCompleted(cursor.getInt(cursor.getColumnIndex(KEY_FORM_COMPLETED)) != 0);
		record.setNeedReview(cursor.getInt(cursor.getColumnIndex(KEY_REVIEW)) != 0);
		record.setRemoteId(cursor.getInt(cursor.getColumnIndex(KEY_REMOTE_ID)));
		cursor.close();

		cursor = db.query(ERROR_TABLE_NAME, null, KEY_FORM_ID + " = ?",
				new String[] { id + "" }, null, null, null);
		while (cursor.moveToNext()) {
			record.addErrorMessage(cursor.getString(cursor
					.getColumnIndex(KEY_FORM_MSG)));
		}
		cursor.close();
		db.close();
		return record;
	}

	public void updateOdkUri(long id, Uri uri) {
		ContentValues cv = new ContentValues();
		//cv.put(KEY_ODK_URI, uri.toString());

		updateFormSubmission(id, cv);
	}

	private void updateFormSubmission(long id, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		db.update(FORM_TABLE_NAME, values, KEY_ID + " = ?", new String[] { id
				+ "" });
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public void updateCompleteStatus(long id, boolean completed) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_FORM_COMPLETED, completed ? 1 : 0);

		updateFormSubmission(id, cv);
	}

	public long createAssociation(String hierarchyPath, String filePath) {
		long id = -1;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(KEY_TO_FORM, hierarchyPath);
			cv.put(KEY_FORM_PATH, filePath);
			id = db.insertOrThrow(ASSOCIATION_TABLE_NAME, null, cv);
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

	public void deleteSubmission(long id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		db.delete(FORM_TABLE_NAME, KEY_ID + " = ?", new String[] { "" + id });
		db.delete(ERROR_TABLE_NAME, KEY_FORM_ID + " = ?", new String[] { ""
				+ id });
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private String getCurrentDateTime() {
		return dateFormat.format(new Date());
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
			db.execSQL(FORM_DB_CREATE);
			db.execSQL(MESSAGE_DB_CREATE);
			db.execSQL(USER_DB_CREATE);
			db.execSQL(ASSOCIATION_DB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onCreate(db);
		}
	}
}
