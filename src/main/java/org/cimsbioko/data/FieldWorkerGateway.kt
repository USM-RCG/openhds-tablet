package org.cimsbioko.data

import android.content.ContentValues
import android.database.Cursor
import org.cimsbioko.App.FieldWorkers
import org.cimsbioko.data.CursorConvert.extractString
import org.cimsbioko.model.core.FieldWorker

const val FIELDWORKER = "fieldworker"

/**
 * Convert FieldWorker to and from database.  FieldWorker-specific queries.
 */
class FieldWorkerGateway internal constructor()
    : Gateway<FieldWorker>(FieldWorkers.CONTENT_ID_URI_BASE, FieldWorkers.COLUMN_FIELD_WORKER_UUID) {

    fun findByExtId(extId: String): Query<FieldWorker> =
            Query<FieldWorker>(this, tableUri, FieldWorkers.COLUMN_FIELD_WORKER_EXTID, extId, FieldWorkers.COLUMN_FIELD_WORKER_UUID)

    override fun getId(entity: FieldWorker): String = entity.uuid!!

    override val entityConverter: CursorConverter<FieldWorker> by lazy { FieldWorkerEntityConverter() }
    override val wrapperConverter: CursorConverter<DataWrapper> by lazy { FieldWorkerWrapperConverter() }
    override val contentValuesConverter: ContentValuesConverter<FieldWorker> by lazy { FieldWorkerContentValuesConverter() }
}

private class FieldWorkerEntityConverter : CursorConverter<FieldWorker> {
    override fun convert(c: Cursor): FieldWorker = FieldWorker().apply {
        extId = extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_EXTID)
        idPrefix = extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX)
        firstName = extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME)
        lastName = extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME)
        passwordHash = extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD)
        uuid = extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_UUID)
    }
}

private class FieldWorkerWrapperConverter : CursorConverter<DataWrapper> {
    override fun convert(c: Cursor): DataWrapper = DataWrapper(
            extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_UUID)!!,
            FIELDWORKER,
            extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_EXTID)!!,
            extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME) + " " + extractString(c, FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME)
    )
}

private class FieldWorkerContentValuesConverter : ContentValuesConverter<FieldWorker> {
    override fun toContentValues(entity: FieldWorker): ContentValues = ContentValues().apply {
        with(entity) {
            put(FieldWorkers.COLUMN_FIELD_WORKER_EXTID, extId)
            put(FieldWorkers.COLUMN_FIELD_WORKER_ID_PREFIX, idPrefix)
            put(FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME, firstName)
            put(FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME, lastName)
            put(FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD, passwordHash)
            put(FieldWorkers.COLUMN_FIELD_WORKER_UUID, uuid)
        }
    }
}