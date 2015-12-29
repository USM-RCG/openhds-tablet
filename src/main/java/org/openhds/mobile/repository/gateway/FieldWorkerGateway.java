package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.FieldWorkers.*;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert FieldWorker to and from database.  FieldWorker-specific queries.
 */
public class FieldWorkerGateway extends Gateway<FieldWorker> {

    public FieldWorkerGateway() {
        super(OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE, COLUMN_FIELD_WORKER_UUID, new FieldWorkerConverter());
    }

    public Query findByExtId(String extId) {
        return new Query(tableUri, COLUMN_FIELD_WORKER_EXTID, extId, COLUMN_FIELD_WORKER_UUID);
    }

    private static class FieldWorkerConverter implements Converter<FieldWorker> {

        @Override
        public FieldWorker fromCursor(Cursor cursor) {
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

            // TODO: this is a temporary hack
            if (null == fieldWorker.getIdPrefix()) {
                fieldWorker.setIdPrefix("99");
            }

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
        public DataWrapper toDataWrapper(ContentResolver contentResolver, FieldWorker fieldWorker, String state) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setExtId(fieldWorker.getExtId());
            dataWrapper.setName(fieldWorker.getFirstName());
            dataWrapper.setCategory(state);
            dataWrapper.setUuid(fieldWorker.getUuid());
            return dataWrapper;
        }
    }
}
