package org.cimsbioko.repository.gateway;

import android.content.ContentValues;
import android.database.Cursor;
import org.cimsbioko.App;
import org.cimsbioko.model.core.FieldWorker;
import org.cimsbioko.repository.Converter;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.repository.Query;

import static org.cimsbioko.App.FieldWorkers.*;
import static org.cimsbioko.repository.RepositoryUtils.extractString;


/**
 * Convert FieldWorker to and from database.  FieldWorker-specific queries.
 */
public class FieldWorkerGateway extends Gateway<FieldWorker> {

    public FieldWorkerGateway() {
        super(App.FieldWorkers.CONTENT_ID_URI_BASE, COLUMN_FIELD_WORKER_UUID, new FieldWorkerConverter());
    }

    public Query findByExtId(String extId) {
        return new Query(tableUri, COLUMN_FIELD_WORKER_EXTID, extId, COLUMN_FIELD_WORKER_UUID);
    }
}

class FieldWorkerConverter implements Converter<FieldWorker> {

    @Override
    public FieldWorker toEntity(Cursor cursor) {
        FieldWorker fieldWorker = new FieldWorker();
        fieldWorker.setExtId(extractString(cursor, COLUMN_FIELD_WORKER_EXTID));
        fieldWorker.setIdPrefix(extractString(cursor, COLUMN_FIELD_WORKER_ID_PREFIX));
        fieldWorker.setFirstName(extractString(cursor, COLUMN_FIELD_WORKER_FIRST_NAME));
        fieldWorker.setLastName(extractString(cursor, COLUMN_FIELD_WORKER_LAST_NAME));
        fieldWorker.setPasswordHash(extractString(cursor, COLUMN_FIELD_WORKER_PASSWORD));
        fieldWorker.setUuid(extractString(cursor, COLUMN_FIELD_WORKER_UUID));
        return fieldWorker;
    }

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

    @Override
    public String getId(FieldWorker fieldWorker) {
        return fieldWorker.getUuid();
    }

    @Override
    public DataWrapper toWrapper(Cursor cursor, String level) {
        DataWrapper dataWrapper = new DataWrapper();
        dataWrapper.setExtId(extractString(cursor, COLUMN_FIELD_WORKER_EXTID));
        dataWrapper.setName(extractString(cursor, COLUMN_FIELD_WORKER_FIRST_NAME));
        dataWrapper.setUuid(extractString(cursor, COLUMN_FIELD_WORKER_UUID));
        dataWrapper.setCategory(level);
        return dataWrapper;
    }
}
