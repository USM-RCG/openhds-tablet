package org.cimsbioko.utilities

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.cimsbioko.App
import org.cimsbioko.model.Form
import org.cimsbioko.model.FormInstance
import org.cimsbioko.provider.FormsProviderAPI
import org.cimsbioko.provider.InstanceProviderAPI
import org.cimsbioko.provider.InstanceProviderAPI.InstanceColumns
import org.cimsbioko.utilities.SQLUtils.makePlaceholders
import java.util.*

object FormsHelper {

    private val INSTANCE_COLUMNS = arrayOf(
        BaseColumns._ID,
        InstanceColumns.JR_FORM_ID,
        InstanceColumns.DISPLAY_NAME,
        InstanceColumns.JR_VERSION,
        InstanceColumns.STATUS,
        InstanceColumns.CAN_EDIT_WHEN_COMPLETE
    )

    private val contentResolver: ContentResolver
        get() = App.instance.contentResolver

    val allUnsentFormInstances: Flow<List<FormInstance>> = flow {
        contentResolver.query(
            InstanceColumns.CONTENT_URI, INSTANCE_COLUMNS,
            "${InstanceColumns.STATUS} != ?", arrayOf(InstanceProviderAPI.STATUS_SUBMITTED), null
        )?.use { c ->
            val result = ArrayList<FormInstance>()
            while (c.moveToNext()) {
                result.add(instanceFromCursor(c))
            }
            emit(result)
        }
    }.flowOn(Dispatchers.IO)

    private fun instanceFromCursor(cursor: Cursor): FormInstance {
        return FormInstance(
            cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)),
                cursor.getString(cursor.getColumnIndex(InstanceColumns.JR_FORM_ID)),
                cursor.getString(cursor.getColumnIndex(InstanceColumns.DISPLAY_NAME)),
                cursor.getString(cursor.getColumnIndex(InstanceColumns.JR_VERSION)),
                cursor.getString(cursor.getColumnIndex(InstanceColumns.STATUS)),
                java.lang.Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(InstanceColumns.CAN_EDIT_WHEN_COMPLETE)))
        )
    }


    /**
     * Retrieves metadata for the form identified by the specified form id.
     *
     * @param formId the form id as specified on the form's data instance element
     * @return a [FormInstance] object containing the matching form's metadata or null if none was found.
     */
    fun getForm(formId: String): Form? {
        var metadata: Form? = null
        val columns = arrayOf(
                BaseColumns._ID,
                FormsProviderAPI.FormsColumns.JR_FORM_ID,
                FormsProviderAPI.FormsColumns.JR_VERSION,
                FormsProviderAPI.FormsColumns.DISPLAY_NAME
        )
        val where = FormsProviderAPI.FormsColumns.JR_FORM_ID + " = ?"
        val whereArgs = arrayOf(formId)
        contentResolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, columns, where, whereArgs, null)?.use { c ->
            if (c.moveToFirst()) {
                metadata = Form(c.getLong(0), c.getString(1), c.getString(2), c.getString(3))
            }
        }
        return metadata
    }

    fun getById(id: Long): FormInstance? {
        return contentResolver.query(
            InstanceColumns.CONTENT_URI, INSTANCE_COLUMNS, "${BaseColumns._ID} = ?", arrayOf(id.toString()), null
        )?.use { c ->
            if (c.moveToNext()) {
                instanceFromCursor(c)
            } else null
        }
    }

    // This could be increased based on the sqlite version: use the lesser anyway to be safe
    private const val SQLITE_MAX_HOST_PARAMETERS = 999

    fun getByIds(ids: Collection<Long>): List<FormInstance> {
        val formInstances = ArrayList<FormInstance>()
        if (!ids.isEmpty()) {
            if (ids.size > SQLITE_MAX_HOST_PARAMETERS) {
                return ids.chunked(SQLITE_MAX_HOST_PARAMETERS).flatMap { getByIds(it) }
            } else {
                val where = String.format("%s IN (%s)", BaseColumns._ID, makePlaceholders(ids.size))
                val idStrings: MutableList<String> = ArrayList(ids.size)
                for (id in ids) {
                    idStrings.add(id.toString())
                }
                contentResolver.query(
                    InstanceColumns.CONTENT_URI, INSTANCE_COLUMNS, where, idStrings.toTypedArray(), null
                )?.use { c ->
                    while (c.moveToNext()) {
                        formInstances.add(instanceFromCursor(c))
                    }
                }
            }
        }
        return formInstances
    }


    /**
     * Retrieves metadata for the blank form identified by the specified form id.
     *
     * @param instance the uri of the instance of interest
     * @return a [FormInstance] object containing the instance's metadata or null if none was found.
     */
    fun getInstance(instance: Uri): FormInstance? {
        var metadata: FormInstance? = null
        contentResolver.query(instance, INSTANCE_COLUMNS, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                metadata = instanceFromCursor(c)
            }
        }
        return metadata
    }

    /**
     * Converts a collection of [FormInstance]s to an array of their ids. This is often handy when working
     * with CIMS Forms's instance content provider.
     *
     * @param forms list of forms
     * @return array of form instance ids in order of the provided forms
     */
    private fun formIds(forms: Collection<FormInstance>): Array<String> = forms.map { it.id.toString() }.toTypedArray()

    /**
     * Deletes the list of [FormInstance]s using CIMS Forms' instance provider. This will delete forms from its db and
     * from the file system.
     *
     * @param forms list of forms
     * @return the number of forms removed
     */
    fun deleteFormInstances(forms: Collection<FormInstance>): Int {
        val where = String.format("%s IN (%s)", BaseColumns._ID, makePlaceholders(forms.size))
        return contentResolver.delete(InstanceColumns.CONTENT_URI, where, formIds(forms))
    }

    fun hasFormsWithIds(formIds: Set<String>): Boolean {
        val projection = arrayOf(FormsProviderAPI.FormsColumns.JR_FORM_ID)
        val where = String.format("%s IN (%s)", FormsProviderAPI.FormsColumns.JR_FORM_ID, makePlaceholders(formIds.size))
        val whereArgs: Array<String> = formIds.toTypedArray()
        val found: MutableSet<String> = HashSet()
        contentResolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, projection, where, whereArgs, null).use { c ->
            if (c != null) {
                while (c.moveToNext()) {
                    found.add(c.getString(0))
                }
            }
        }
        return found.containsAll(formIds)
    }
}