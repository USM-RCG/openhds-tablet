package org.cimsbioko.data

import android.net.Uri
import org.cimsbioko.App
import org.cimsbioko.navconfig.UsedByJSConfig

/**
 * Base class for simplistic data mappers supporting CRUD operations
 */
abstract class Gateway<T> internal constructor(
        val tableUri: Uri,
        private val idColumnName: String
) {

    @UsedByJSConfig
    fun insertOrUpdate(entity: T): Boolean {
        val resolver = App.getApp().contentResolver
        val contentValues = contentValuesConverter.toContentValues(entity)
        val id = getId(entity)
        return if (findById(id).exists()) {
            resolver.update(tableUri, contentValues, WhereUtils.buildWhereStatement(arrayOf(idColumnName), WhereUtils.EQUALS), arrayOf(id))
            false
        } else {
            null != resolver.insert(tableUri, contentValues)
        }
    }

    abstract fun getId(entity: T): String
    abstract val entityConverter: CursorConverter<T>
    abstract val wrapperConverter: CursorConverter<DataWrapper?>
    abstract val contentValuesConverter: ContentValuesConverter<T>

    // find entities with given id
    fun findById(id: String): Query<T> {
        return Query(this, tableUri, idColumnName, id, idColumnName)
    }

    // find entities ordered by id, might be huge
    fun findAll(): Query<T> {
        return Query(this, tableUri, null, null, idColumnName)
    }

}