package org.cimsbioko.repository.gateway;

import android.content.ContentValues;
import android.database.Cursor;
import org.cimsbioko.App;
import org.cimsbioko.model.core.LocationHierarchy;
import org.cimsbioko.repository.ContentValuesConverter;
import org.cimsbioko.repository.CursorConverter;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.repository.Query;

import java.util.HashMap;
import java.util.Map;

import static org.cimsbioko.App.HierarchyItems.*;
import static org.cimsbioko.repository.RepositoryUtils.extractString;

/**
 * Convert LocationHierarchy items to and from database.  LocationHierarchy-specific queries.
 */
public class LocationHierarchyGateway extends Gateway<LocationHierarchy> {

    private static final LocationHierarchyEntityConverter ENTITY_CONVERTER = new LocationHierarchyEntityConverter();
    private static final Map<String, LocationHierarchyWrapperConverter> WRAPPER_CONVERTERS = new HashMap<>();
    private static final LocationHierarchyContentValuesConverter CONTENT_VALUES_CONVERTER = new LocationHierarchyContentValuesConverter();

    public LocationHierarchyGateway() {
        super(App.HierarchyItems.CONTENT_ID_URI_BASE, COLUMN_HIERARCHY_UUID);
    }

    public Query findByLevel(String level) {
        return new Query(tableUri, COLUMN_HIERARCHY_LEVEL, level, COLUMN_HIERARCHY_UUID);
    }

    public Query findByParent(String parentId) {
        return new Query(tableUri, COLUMN_HIERARCHY_PARENT, parentId, COLUMN_HIERARCHY_EXTID);
    }

    @Override
    String getId(LocationHierarchy entity) {
        return entity.getUuid();
    }

    @Override
    CursorConverter<LocationHierarchy> getEntityConverter() {
        return ENTITY_CONVERTER;
    }

    @Override
    CursorConverter<DataWrapper> getWrapperConverter(String level) {
        if (WRAPPER_CONVERTERS.containsKey(level)) {
            return WRAPPER_CONVERTERS.get(level);
        } else {
            LocationHierarchyWrapperConverter converter = new LocationHierarchyWrapperConverter(level);
            WRAPPER_CONVERTERS.put(level, converter);
            return converter;
        }
    }

    @Override
    ContentValuesConverter<LocationHierarchy> getContentValuesConverter() {
        return CONTENT_VALUES_CONVERTER;
    }
}

class LocationHierarchyEntityConverter implements CursorConverter<LocationHierarchy> {

    @Override
    public LocationHierarchy convert(Cursor c) {
        LocationHierarchy locationHierarchy = new LocationHierarchy();
        locationHierarchy.setUuid(extractString(c, COLUMN_HIERARCHY_UUID));
        locationHierarchy.setExtId(extractString(c, COLUMN_HIERARCHY_EXTID));
        locationHierarchy.setName(extractString(c, COLUMN_HIERARCHY_NAME));
        locationHierarchy.setLevel(extractString(c, COLUMN_HIERARCHY_LEVEL));
        locationHierarchy.setParentUuid(extractString(c, COLUMN_HIERARCHY_PARENT));
        locationHierarchy.setAttrs(extractString(c, COLUMN_HIERARCHY_ATTRS));
        return locationHierarchy;
    }
}

class LocationHierarchyWrapperConverter implements CursorConverter<DataWrapper> {

    private final String level;

    public LocationHierarchyWrapperConverter(String level) {
        this.level = level;
    }

    @Override
    public DataWrapper convert(Cursor c) {
        DataWrapper dataWrapper = new DataWrapper();
        dataWrapper.setExtId(extractString(c, COLUMN_HIERARCHY_EXTID));
        dataWrapper.setUuid(extractString(c, COLUMN_HIERARCHY_UUID));
        dataWrapper.setName(extractString(c, COLUMN_HIERARCHY_NAME));
        dataWrapper.setCategory(level);
        return dataWrapper;
    }
}

class LocationHierarchyContentValuesConverter implements ContentValuesConverter<LocationHierarchy> {

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
}
