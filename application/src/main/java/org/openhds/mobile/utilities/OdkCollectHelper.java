package org.openhds.mobile.utilities;

import static org.openhds.mobile.provider.InstanceProviderAPI.InstanceColumns.CONTENT_URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.content.ContentValues;
import org.openhds.mobile.provider.InstanceProviderAPI;
import org.openhds.mobile.model.form.FormInstance;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class OdkCollectHelper {

    public static List<FormInstance> getAllUnsentFormInstances(ContentResolver resolver) {

        ArrayList<FormInstance> formInstances = new ArrayList<FormInstance>();

        Cursor cursor = resolver.query(CONTENT_URI, new String[] {
                InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
                InstanceProviderAPI.InstanceColumns._ID,
                InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                InstanceProviderAPI.InstanceColumns.STATUS}, InstanceProviderAPI.InstanceColumns.STATUS+" != ?",
                            new String[] {InstanceProviderAPI.STATUS_SUBMITTED}, null);

        if (null == cursor) {
            return null;
        }

        while (cursor.moveToNext()) {

            FormInstance formInstance = new FormInstance();
            String filePath;
            String formName;
            String fileName;

            Uri uri;

            filePath = cursor
                    .getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

            uri = Uri.withAppendedPath(CONTENT_URI, cursor.getString(cursor
                    .getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));

            String formStatus = cursor
                    .getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));

            formName = cursor
                    .getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));

            fileName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));

            formInstance.setFilePath(filePath);
            formInstance.setUriString(uri.toString());
            formInstance.setFormName(formName);
            formInstance.setFileName(fileName);

            formInstances.add(formInstance);

        }
        cursor.close();
        return formInstances;
    }



	public static List<FormInstance> getAllFormInstances(
			ContentResolver resolver) {

		ArrayList<FormInstance> formInstances = new ArrayList<FormInstance>();

		Cursor cursor = resolver.query(CONTENT_URI, new String[] {
				InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
				InstanceProviderAPI.InstanceColumns._ID,
                InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME}, null, null, null);

        if (null == cursor) {
            return null;
        }

		while (cursor.moveToNext()) {

			FormInstance formInstance = new FormInstance();
			String filePath;
            String formName;
            String fileName;

			Uri uri;

			filePath = cursor
					.getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

			uri = Uri.withAppendedPath(CONTENT_URI, cursor.getString(cursor
					.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));

            formName = cursor
                    .getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));

            fileName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));

			formInstance.setFilePath(filePath);
            formInstance.setFormName(formName);
            formInstance.setFileName(fileName);

            formInstance.setUriString(uri.toString());

			formInstances.add(formInstance);

		}
		cursor.close();
		return formInstances;
	}

    public static void deleteInstance(ContentResolver resolver, Uri uri, String filePath) {
        int result = resolver.delete(uri, InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH+"=?", new String[]{filePath});
        result = result;
    }

    public static void setStatusSubmitted(ContentResolver resolver, Uri uri) {

        ContentValues cv = new ContentValues();
        cv.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
        resolver.update(uri, cv, null, null);

    }

    public static void setStatusIncomplete(ContentResolver resolver, Uri uri) {

        ContentValues cv = new ContentValues();
        cv.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);
        resolver.update(uri, cv, null, null);

    }

    public static void setStatusComplete(ContentResolver resolver, Uri uri) {

        ContentValues cv = new ContentValues();
        cv.put(InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.STATUS_COMPLETE);
        resolver.update(uri, cv, null, null);

    }

    private static String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    public static List<FormInstance> getFormInstancesByPath(ContentResolver resolver, Collection<String> ids) {

        ArrayList<FormInstance> formInstances = new ArrayList<FormInstance>();

        Cursor cursor = resolver.query(CONTENT_URI, new String[] {
                        InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
                        InstanceProviderAPI.InstanceColumns._ID,
                        InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
                        InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                        InstanceProviderAPI.InstanceColumns.STATUS},
                InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH
                        + " IN (" + makePlaceholders(ids.size()) + ")",
                ids.toArray(new String[]{}), null);

        if (null == cursor) {
            return null;
        }

        while (cursor.moveToNext()) {

            FormInstance formInstance = new FormInstance();
            String filePath;
            String formName;
            String fileName;

            Uri uri;

            filePath = cursor
                    .getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

            uri = Uri.withAppendedPath(CONTENT_URI, cursor.getString(cursor
                    .getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));

            String formStatus = cursor
                    .getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));

            formName = cursor
                    .getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));

            fileName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));

            formInstance.setFilePath(filePath);
            formInstance.setUriString(uri.toString());
            formInstance.setFormName(formName);
            formInstance.setFileName(fileName);

            formInstances.add(formInstance);

        }
        cursor.close();
        return formInstances;
    }
}
