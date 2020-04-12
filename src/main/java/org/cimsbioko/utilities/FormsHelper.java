package org.cimsbioko.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import org.cimsbioko.App;
import org.cimsbioko.model.form.Form;
import org.cimsbioko.model.form.FormInstance;
import org.cimsbioko.provider.FormsProviderAPI;
import org.cimsbioko.provider.InstanceProviderAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.cimsbioko.provider.InstanceProviderAPI.InstanceColumns.CONTENT_URI;
import static org.cimsbioko.utilities.SQLUtils.makePlaceholders;

public class FormsHelper {

    private static final String[] INSTANCE_COLUMNS = {
            InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
            InstanceProviderAPI.InstanceColumns._ID,
            InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
            InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
            InstanceProviderAPI.InstanceColumns.STATUS,
            InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE};

    public static ContentResolver getContentResolver() {
        return App.getApp().getContentResolver();
    }

    public static List<FormInstance> getAllUnsentFormInstances() {

        ArrayList<FormInstance> formInstances = new ArrayList<>();
        Cursor cursor = getContentResolver().query(CONTENT_URI, INSTANCE_COLUMNS,
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
        formInstance.setCanEditWhenComplete(Boolean.parseBoolean(
                        cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE))));
        return formInstance;
    }

    public static List<FormInstance> getByPaths(Collection<String> formPaths) {
        ArrayList<FormInstance> formInstances = new ArrayList<>();
        if (!formPaths.isEmpty()) {
            String where = String.format("%s IN (%s)", InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, makePlaceholders(formPaths.size()));
            String[] whereArgs = formPaths.toArray(new String[0]);
            Cursor cursor = getContentResolver().query(CONTENT_URI, INSTANCE_COLUMNS, where, whereArgs, null);
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
     * Registers the given XML file as a form instance with CIMS Forms.
     *
     * @param instance
     * @param name
     * @param id
     * @param version
     * @return the {@link Uri} for the registered form instance
     */
    public static Uri registerInstance(File instance, String name, String id, String version) {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, instance.getAbsolutePath());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, name);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, id);
        values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, version);
        return getContentResolver().insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
    }

    /**
     * Retrieves metadata for the form identified by the specified form id.
     *
     * @param formId   the form id as specified on the form's data instance element
     * @return a {@link FormInstance} object containing the matching form's metadata or null if none was found.
     */
    public static Form getForm(String formId) {
        Form metadata = null;
        final String[] columns = {
                FormsProviderAPI.FormsColumns._ID,
                FormsProviderAPI.FormsColumns.JR_FORM_ID,
                FormsProviderAPI.FormsColumns.JR_VERSION,
                FormsProviderAPI.FormsColumns.DISPLAY_NAME
        };
        final String where = FormsProviderAPI.FormsColumns.JR_FORM_ID + " = ?";
        final String[] whereArgs = {formId};
        Cursor cursor = getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, columns, where, whereArgs, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    metadata = new Form(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
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
     * @param instance the uri of the instance of interest
     * @return a {@link FormInstance} object containing the instance's metadata or null if none was found.
     */
    public static FormInstance getInstance(Uri instance) {
        FormInstance metadata = null;
        Cursor cursor = getContentResolver().query(instance, INSTANCE_COLUMNS, null, null, null);
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

    /**
     * Converts a collection of {@link FormInstance}s to an array of file system paths. This is often handy when working
     * with CIMS Forms's instance content provider.
     *
     * @param forms list of forms
     * @return array of file system paths in order of the provided forms
     */
    public static String[] formPaths(Collection<FormInstance> forms) {
        String[] paths = new String[forms.size()];
        int index = 0;
        for (FormInstance form : forms)
            paths[index++] = form.getFilePath();
        return paths;
    }

    /**
     * Deletes the list of {@link FormInstance}s using CIMS Forms' instance provider. This will delete forms from its db and
     * from the file system.
     *
     * @param forms    list of forms
     * @return the number of forms removed
     */
    public static int deleteFormInstances(Collection<FormInstance> forms) {
        String where = String.format("%s IN (%s)", InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, makePlaceholders(forms.size()));
        return getContentResolver().delete(CONTENT_URI, where, formPaths(forms));
    }
}
