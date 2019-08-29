package org.cimsbioko.repository;

import android.content.ContentValues;
import android.database.Cursor;
import org.cimsbioko.App;
import org.cimsbioko.model.core.FieldWorker;

import static org.cimsbioko.App.FieldWorkers.*;
import static org.cimsbioko.repository.CursorConvert.extractString;


/**
 * Convert FieldWorker to and from database.  FieldWorker-specific queries.
 */
public class FieldWorkerGateway extends Gateway<FieldWorker> {

    private static final FieldWorkerEntityConverter ENTITY_CONVERTER = new FieldWorkerEntityConverter();
    private static final FieldWorkerWrapperConverter WRAPPER_CONVERTER = new FieldWorkerWrapperConverter();
    private static final FieldWorkerContentValuesConverter CONTENT_VALUES_CONVERTER = new FieldWorkerContentValuesConverter();

    FieldWorkerGateway() {
        super(App.FieldWorkers.CONTENT_ID_URI_BASE, COLUMN_FIELD_WORKER_UUID);
    }

    public Query<FieldWorker> findByExtId(String extId) {
        return new Query<>(this, tableUri, COLUMN_FIELD_WORKER_EXTID, extId, COLUMN_FIELD_WORKER_UUID);
    }

    @Override
    String getId(FieldWorker entity) {
        return entity.getUuid();
    }

    @Override
    public CursorConverter<FieldWorker> getEntityConverter() {
        return ENTITY_CONVERTER;
    }

    @Override
    public CursorConverter<DataWrapper> getWrapperConverter() {
        return WRAPPER_CONVERTER;
    }

    @Override
    ContentValuesConverter<FieldWorker> getContentValuesConverter() {
        return CONTENT_VALUES_CONVERTER;
    }
}

class FieldWorkerEntityConverter implements CursorConverter<FieldWorker> {
    @Override
    public FieldWorker convert(Cursor c) {
        FieldWorker fieldWorker = new FieldWorker();
        fieldWorker.setExtId(extractString(c, COLUMN_FIELD_WORKER_EXTID));
        fieldWorker.setIdPrefix(extractString(c, COLUMN_FIELD_WORKER_ID_PREFIX));
        fieldWorker.setFirstName(extractString(c, COLUMN_FIELD_WORKER_FIRST_NAME));
        fieldWorker.setLastName(extractString(c, COLUMN_FIELD_WORKER_LAST_NAME));
        fieldWorker.setPasswordHash(extractString(c, COLUMN_FIELD_WORKER_PASSWORD));
        fieldWorker.setUuid(extractString(c, COLUMN_FIELD_WORKER_UUID));
        return fieldWorker;
    }
}

class FieldWorkerWrapperConverter implements CursorConverter<DataWrapper> {
    @Override
    public DataWrapper convert(Cursor c) {
        DataWrapper dataWrapper = new DataWrapper();
        dataWrapper.setExtId(extractString(c, COLUMN_FIELD_WORKER_EXTID));
        dataWrapper.setName(extractString(c, COLUMN_FIELD_WORKER_FIRST_NAME));
        dataWrapper.setUuid(extractString(c, COLUMN_FIELD_WORKER_UUID));
        dataWrapper.setCategory("fieldworker");
        return dataWrapper;
    }
}

class FieldWorkerContentValuesConverter implements ContentValuesConverter<FieldWorker> {
    @Override
    public ContentValues toContentValues(FieldWorker fieldWorker) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FIELD_WORKER_EXTID, fieldWorker.getExtId());
        contentValues.put(COLUMN_FIELD_WORKER_ID_PREFIX, fieldWorker.getIdPrefix());
        contentValues.put(COLUMN_FIELD_WORKER_FIRST_NAME, fieldWorker.getFirstName());
        contentValues.put(COLUMN_FIELD_WORKER_LAST_NAME, fieldWorker.getLastName());
        contentValues.put(COLUMN_FIELD_WORKER_PASSWORD, fieldWorker.getPasswordHash());
        contentValues.put(COLUMN_FIELD_WORKER_UUID, fieldWorker.getUuid());
        return contentValues;
    }
}
