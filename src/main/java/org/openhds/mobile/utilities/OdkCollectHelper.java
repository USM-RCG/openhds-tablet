package org.openhds.mobile.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.provider.FormsProviderAPI;
import org.openhds.mobile.provider.InstanceProviderAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.openhds.mobile.provider.InstanceProviderAPI.InstanceColumns.CONTENT_URI;
import static org.openhds.mobile.utilities.SQLUtils.makePlaceholders;

public class OdkCollectHelper {

    private static final String[] INSTANCE_COLUMNS = {
            InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
            InstanceProviderAPI.InstanceColumns._ID,
            InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
            InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
            InstanceProviderAPI.InstanceColumns.STATUS};

    public static List<FormInstance> getAllUnsentFormInstances(ContentResolver resolver) {

        ArrayList<FormInstance> formInstances = new ArrayList<>();
        Cursor cursor = resolver.query(CONTENT_URI, INSTANCE_COLUMNS,
                InstanceProviderAPI.InstanceColumns.STATUS + " != ?",
                new String[]{InstanceProviderAPI.STATUS_SUBMITTED}, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    formInstances.add(instanceFromCursor(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return formInstances;
    }

    private static FormInstance instanceFromCursor(Cursor cursor) {
        FormInstance formInstance = new FormInstance();
        Uri uri = Uri.withAppendedPath(CONTENT_URI, cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
        formInstance.setUriString(uri.toString());
        formInstance.setFilePath(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)));
        formInstance.setFormName(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID)));
        formInstance.setFileName(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME)));
        formInstance.setStatus(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS)));
        return formInstance;
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

    public static List<FormInstance> getByPaths(ContentResolver resolver, Collection<String> formPaths) {
        ArrayList<FormInstance> formInstances = new ArrayList<>();
        if (!formPaths.isEmpty()) {
            String where = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH +
                    " IN (" + makePlaceholders(formPaths.size()) + ")";
            String[] whereArgs = formPaths.toArray(new String[formPaths.size()]);
            Cursor cursor = resolver.query(CONTENT_URI, INSTANCE_COLUMNS, where, whereArgs, null);
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        formInstances.add(instanceFromCursor(cursor));
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return formInstances;
    }

    /**
     * Registers the given XML file as a form instance with ODK Collect.
     *
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
     * Retrieves metadata for the blank form identified by the specified form id.
     *
     * @param resolver
     * @param formId   the form id as specified on the form's data instance element
     * @return a {@link FormInstance} object containing the matching form's metadata or null if none was found.
     */
    public static FormInstance getBlankInstance(ContentResolver resolver, String formId) {
        FormInstance metadata = null;
        final String[] columns = {FormsProviderAPI.FormsColumns.JR_FORM_ID,
                FormsProviderAPI.FormsColumns.FORM_FILE_PATH,
                FormsProviderAPI.FormsColumns.JR_VERSION};
        final String where = FormsProviderAPI.FormsColumns.JR_FORM_ID + " = ?";
        final String[] whereArgs = {formId};
        Cursor cursor = resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, columns, where, whereArgs, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    metadata = new FormInstance();
                    metadata.setFormName(cursor.getString(0));
                    metadata.setFilePath(cursor.getString(1));
                    metadata.setFormVersion(cursor.getString(2));
                }
            } finally {
                cursor.close();
            }
        }
        return metadata;
    }

    /**
     * Retrieves metadata for the blank form identified by the specified form id.
     *
     * @param resolver
     * @param instance the uri of the instance of interest
     * @return a {@link FormInstance} object containing the instance's metadata or null if none was found.
     */
    public static FormInstance getInstance(ContentResolver resolver, Uri instance) {
        FormInstance metadata = null;
        Cursor cursor = resolver.query(instance, INSTANCE_COLUMNS, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    metadata = instanceFromCursor(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return metadata;
    }
}
