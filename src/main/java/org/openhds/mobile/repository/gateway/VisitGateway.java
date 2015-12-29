package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;

import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_DATE;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_UUID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_EXTID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_FIELDWORKER_UUID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_LOCATION_UUID;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert Visit to and from database.  Visit-specific queries.
 */
public class VisitGateway extends Gateway<Visit> {

    public VisitGateway() {
        super(OpenHDS.Visits.CONTENT_ID_URI_BASE, COLUMN_VISIT_EXTID, new VisitConverter());
    }

    private static class VisitConverter implements Converter<Visit> {

        @Override
        public Visit fromCursor(Cursor cursor) {
            Visit visit = new Visit();

            visit.setUuid(extractString(cursor, COLUMN_VISIT_UUID));
            visit.setExtId(extractString(cursor, COLUMN_VISIT_EXTID));
            visit.setVisitDate(extractString(cursor, COLUMN_VISIT_DATE));
            visit.setLocationUuid(extractString(cursor, COLUMN_VISIT_LOCATION_UUID));
            visit.setFieldWorkerUuid(extractString(cursor, COLUMN_VISIT_FIELDWORKER_UUID));

            return visit;
        }

        @Override
        public ContentValues toContentValues(Visit visit) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_VISIT_EXTID, visit.getExtId());
            contentValues.put(COLUMN_VISIT_UUID, visit.getUuid());
            contentValues.put(COLUMN_VISIT_DATE, visit.getVisitDate());
            contentValues.put(COLUMN_VISIT_LOCATION_UUID, visit.getLocationUuid());
            contentValues.put(COLUMN_VISIT_FIELDWORKER_UUID, visit.getFieldWorkerUuid());

            return contentValues;
        }

        @Override
        public String getId(Visit visit) {
            return visit.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Visit visit, String state) {
            DataWrapper dataWrapper = new DataWrapper();

            dataWrapper.setUuid(visit.getUuid());
            dataWrapper.setExtId(visit.getExtId());
            dataWrapper.setName(visit.getLocationUuid());
            dataWrapper.setCategory(state);

            return dataWrapper;
        }
    }
}
