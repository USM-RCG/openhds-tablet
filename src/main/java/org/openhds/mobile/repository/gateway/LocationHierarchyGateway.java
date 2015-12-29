package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.HierarchyItems.*;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert LocationHierarchy items to and from database.  LocationHierarchy-specific queries.
 */
public class LocationHierarchyGateway extends Gateway<LocationHierarchy> {

    public LocationHierarchyGateway() {
        super(OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE, COLUMN_HIERARCHY_UUID, new LocationHierarchyConverter());
    }

    public Query findByLevel(String level) {
        return new Query(tableUri, COLUMN_HIERARCHY_LEVEL, level, COLUMN_HIERARCHY_UUID);
    }

    public Query findByExtId(String extId) {
        return new Query(tableUri, COLUMN_HIERARCHY_EXTID, extId, COLUMN_HIERARCHY_UUID);
    }

    public Query findByParent(String parentId) {
        return new Query(tableUri, COLUMN_HIERARCHY_PARENT, parentId, COLUMN_HIERARCHY_UUID);
    }

    private static class LocationHierarchyConverter implements Converter<LocationHierarchy> {

        @Override
        public LocationHierarchy fromCursor(Cursor cursor) {
            LocationHierarchy locationHierarchy = new LocationHierarchy();

            locationHierarchy.setUuid(extractString(cursor, COLUMN_HIERARCHY_UUID));
            locationHierarchy.setExtId(extractString(cursor, COLUMN_HIERARCHY_EXTID));
            locationHierarchy.setName(extractString(cursor, COLUMN_HIERARCHY_NAME));
            locationHierarchy.setLevel(extractString(cursor, COLUMN_HIERARCHY_LEVEL));
            locationHierarchy.setParentUuid(extractString(cursor, COLUMN_HIERARCHY_PARENT));

            return locationHierarchy;
        }

        @Override
        public ContentValues toContentValues(LocationHierarchy locationHierarchy) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_HIERARCHY_UUID, locationHierarchy.getUuid());
            contentValues.put(COLUMN_HIERARCHY_EXTID, locationHierarchy.getExtId());
            contentValues.put(COLUMN_HIERARCHY_NAME, locationHierarchy.getName());
            contentValues.put(COLUMN_HIERARCHY_LEVEL, locationHierarchy.getLevel());
            contentValues.put(COLUMN_HIERARCHY_PARENT, locationHierarchy.getParentUuid());

            return contentValues;
        }

        @Override
        public String getId(LocationHierarchy locationHierarchy) {
            return locationHierarchy.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, LocationHierarchy locationHierarchy, String state) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setExtId(locationHierarchy.getExtId());
            dataWrapper.setUuid(locationHierarchy.getUuid());
            dataWrapper.setName(locationHierarchy.getName());
            dataWrapper.setCategory(state);
            return dataWrapper;
        }
    }
}
