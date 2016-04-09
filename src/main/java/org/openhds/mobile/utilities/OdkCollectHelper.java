package org.openhds.mobile.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.provider.FormsProviderAPI;
import org.openhds.mobile.provider.InstanceProviderAPI;
import org.openhds.mobile.repository.Query;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.openhds.mobile.provider.InstanceProviderAPI.InstanceColumns.CONTENT_URI;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE_WILD_CARD;

public class OdkCollectHelper {

    public static List<FormInstance> getAllUnsentFormInstances(ContentResolver resolver) {

        ArrayList<FormInstance> formInstances = new ArrayList<>();
        Cursor cursor = resolver.query(CONTENT_URI, new String[]{
                        InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
                        InstanceProviderAPI.InstanceColumns._ID,
                        InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
                        InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                        InstanceProviderAPI.InstanceColumns.STATUS}, InstanceProviderAPI.InstanceColumns.STATUS + " != ?",
                new String[]{InstanceProviderAPI.STATUS_SUBMITTED}, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    FormInstance formInstance = new FormInstance();
                    String filePath, formName, fileName;
                    filePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                    Uri uri = Uri.withAppendedPath(CONTENT_URI, cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                    formName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                    fileName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                    formInstance.setFilePath(filePath);
                    formInstance.setUriString(uri.toString());
                    formInstance.setFormName(formName);
                    formInstance.setFileName(fileName);
                    formInstances.add(formInstance);
                }
            } finally {
                cursor.close();
            }
        }

        return formInstances;
    }

    public static List<FormInstance> getAllFormInstances(ContentResolver resolver) {

        ArrayList<FormInstance> formInstances = new ArrayList<>();
        Cursor cursor = resolver.query(CONTENT_URI, new String[]{
                InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
                InstanceProviderAPI.InstanceColumns._ID,
                InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME}, null, null, null);

        if (null == cursor) {
            return null;
        }

        while (cursor.moveToNext()) {
            FormInstance formInstance = new FormInstance();
            String filePath, formName, fileName;
            filePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
            Uri uri = Uri.withAppendedPath(CONTENT_URI, cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
            formName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
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
        resolver.delete(uri, InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?", new String[]{filePath});
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

    public static String makePlaceholders(int len) {
        if (len < 1) {
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

    public static List<FormInstance> getByPaths(ContentResolver resolver, Collection<String> ids) {
        ArrayList<FormInstance> formInstances = new ArrayList<>();
        Cursor cursor = resolver.query(CONTENT_URI, new String[]{
                        InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
                        InstanceProviderAPI.InstanceColumns._ID,
                        InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
                        InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                        InstanceProviderAPI.InstanceColumns.STATUS},
                InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH
                        + " IN (" + makePlaceholders(ids.size()) + ")",
                ids.toArray(new String[ids.size()]), null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    FormInstance formInstance = new FormInstance();
                    String filePath, formName, fileName;
                    filePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                    Uri uri = Uri.withAppendedPath(CONTENT_URI, cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                    formName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                    fileName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                    formInstance.setFilePath(filePath);
                    formInstance.setUriString(uri.toString());
                    formInstance.setFormName(formName);
                    formInstance.setFileName(fileName);
                    formInstances.add(formInstance);
                }
            } finally {
                cursor.close();
            }
        }
        return formInstances;
    }

    public static List<String> getSentFormPaths(ContentResolver resolver, Collection<String> ids) {
        ArrayList<String> sentPaths = new ArrayList<>();
        for (String path : ids) {
            Query query = new Query(CONTENT_URI,
                    new String[]{InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, InstanceProviderAPI.InstanceColumns.STATUS},
                    new String[]{path, InstanceProviderAPI.STATUS_SUBMITTED}, null, "=");
            Cursor cursor = query.select(resolver);
            if (null == cursor) {
                return null;
            }
            if (cursor.moveToFirst()) {
                String sentPath;
                sentPath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                sentPaths.add(sentPath);
            }
            cursor.close();
        }
        return sentPaths;
    }

    /**
     * Registers the given XML file as a form instance with ODK Collect.
     * @param resolver
     * @param instance
     * @param name
     * @param id
     * @param version
     * @return the {@link Uri} for the registered form instance
     */
    public static Uri registerInstance(ContentResolver resolver, File instance, String name, String id, String version) {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, instance.getAbsolutePath());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, name);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, id);
        values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, version);
        return resolver.insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
    }

    /**
     * Returns the path to the completed form identified by the specified {@link Uri}.
     *
     * @return true if the form is complete, false otherwise
     */
    public static String completedFormPath(ContentResolver resolver, Uri formUri) throws FileNotFoundException {

        final String[] columns = {InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH};
        final String where = InstanceProviderAPI.InstanceColumns.STATUS + "=?";
        final String[] whereArgs = {InstanceProviderAPI.STATUS_COMPLETE};
        Cursor cursor = resolver.query(formUri, columns, where, whereArgs, null);
        try {
            if (cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        throw new FileNotFoundException("no completed form for uri " + formUri);
    }


    /**
     * Retrieves metadata for the blank form identified by the specified form id.
     *
     * @param resolver
     * @param formId the form id as specified on the form's data instance element
     * @return a {@link FormInstance} object containing the matching form's metadata or null if none was found.
     */
    public static FormInstance getBlankInstance(ContentResolver resolver, String formId) {
        FormInstance metadata;
        final String[] columns = {FormsProviderAPI.FormsColumns.JR_FORM_ID,
                FormsProviderAPI.FormsColumns.FORM_FILE_PATH,
                FormsProviderAPI.FormsColumns.JR_VERSION};
        final String where = FormsProviderAPI.FormsColumns.JR_FORM_ID + " " + LIKE + " ?";
        final String[] whereArgs = {formId + LIKE_WILD_CARD};
        Cursor cursor = resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, columns, where, whereArgs, null);
        try {
            if (cursor.moveToFirst()) {
                metadata = new FormInstance();
                metadata.setFormName(cursor.getString(0));
                metadata.setFilePath(cursor.getString(1));
                metadata.setFormVersion(cursor.getString(2));
            } else {
                metadata = null;
            }
        } finally {
            cursor.close();
        }
        return metadata;
    }
}
