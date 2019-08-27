package org.cimsbioko.repository.gateway;

import android.content.ContentValues;
import android.database.Cursor;
import org.cimsbioko.App;
import org.cimsbioko.model.core.LocationHierarchy;
import org.cimsbioko.repository.Converter;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.repository.Query;

import static org.cimsbioko.App.HierarchyItems.*;
import static org.cimsbioko.repository.RepositoryUtils.extractString;

/**
 * Convert LocationHierarchy items to and from database.  LocationHierarchy-specific queries.
 */
public class LocationHierarchyGateway extends Gateway<LocationHierarchy> {

    public LocationHierarchyGateway() {
        super(App.HierarchyItems.CONTENT_ID_URI_BASE, COLUMN_HIERARCHY_UUID, new LocationHierarchyConverter());
    }

    public Query findByLevel(String level) {
        return new Query(tableUri, COLUMN_HIERARCHY_LEVEL, level, COLUMN_HIERARCHY_UUID);
    }

    public Query findByExtId(String extId) {
        return new Query(tableUri, COLUMN_HIERARCHY_EXTID, extId, COLUMN_HIERARCHY_UUID);
    }

    public Query findByParent(String parentId) {
        return new Query(tableUri, COLUMN_HIERARCHY_PARENT, parentId, COLUMN_HIERARCHY_EXTID);
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
            locationHierarchy.setAttrs(extractString(cursor, COLUMN_HIERARCHY_ATTRS));

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
            contentValues.put(COLUMN_HIERARCHY_ATTRS, locationHierarchy.getAttrs());

            return contentValues;
        }

        @Override
        public String getId(LocationHierarchy locationHierarchy) {
            return locationHierarchy.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(LocationHierarchy locationHierarchy, String level) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setExtId(locationHierarchy.getExtId());
            dataWrapper.setUuid(locationHierarchy.getUuid());
            dataWrapper.setName(locationHierarchy.getName());
            dataWrapper.setCategory(level);
            return dataWrapper;
        }
    }
}
