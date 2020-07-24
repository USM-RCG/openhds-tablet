package org.cimsbioko.data;

import android.content.ContentValues;
import android.database.Cursor;

import org.cimsbioko.App;
import org.cimsbioko.R;
import org.cimsbioko.model.core.Location;
import org.jetbrains.annotations.NotNull;

import static org.cimsbioko.App.Locations.COLUMN_LOCATION_ATTRS;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_DESCRIPTION;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_EXTID;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_HIERARCHY_UUID;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_LATITUDE;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_LONGITUDE;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_NAME;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_UUID;
import static org.cimsbioko.navconfig.Hierarchy.HOUSEHOLD;
import static org.cimsbioko.data.CursorConvert.extractString;

/**
 * Convert Locations to and from database.  Location-specific queries.
 */
public class LocationGateway extends Gateway<Location> {

    private static final LocationEntityConverter ENTITY_CONVERTER = new LocationEntityConverter();
    private static final LocationWrapperConverter WRAPPER_CONVERTER = new LocationWrapperConverter();
    private static final LocationContentValuesConverter CONTENT_VALUES_CONVERTER = new LocationContentValuesConverter();

    LocationGateway() {
        super(App.Locations.CONTENT_ID_URI_BASE, COLUMN_LOCATION_UUID);
    }

    public Query<Location> findByHierarchy(String hierarchyId) {
        return new Query<>(this, tableUri, COLUMN_LOCATION_HIERARCHY_UUID, hierarchyId, COLUMN_LOCATION_EXTID);
    }

    @Override
    String getId(Location entity) {
        return entity.getUuid();
    }

    @Override
    public CursorConverter<Location> getEntityConverter() {
        return ENTITY_CONVERTER;
    }

    @Override
    public CursorConverter<DataWrapper> getWrapperConverter() {
        return WRAPPER_CONVERTER;
    }

    @Override
    ContentValuesConverter<Location> getContentValuesConverter() {
        return CONTENT_VALUES_CONVERTER;
    }
}

class LocationEntityConverter implements CursorConverter<Location> {
    @Override
    @NotNull
    public Location convert(@NotNull Cursor c) {
        Location location = new Location();
        location.setUuid(extractString(c, COLUMN_LOCATION_UUID));
        location.setExtId(extractString(c, COLUMN_LOCATION_EXTID));
        location.setHierarchyUuid(extractString(c, COLUMN_LOCATION_HIERARCHY_UUID));
        location.setLatitude(extractString(c, COLUMN_LOCATION_LATITUDE));
        location.setLongitude(extractString(c, COLUMN_LOCATION_LONGITUDE));
        location.setName(extractString(c, COLUMN_LOCATION_NAME));
        location.setDescription(extractString(c, COLUMN_LOCATION_DESCRIPTION));
        location.setLongitude(extractString(c, COLUMN_LOCATION_LONGITUDE));
        location.setLatitude(extractString(c, COLUMN_LOCATION_LATITUDE));
        location.setAttrs(extractString(c, COLUMN_LOCATION_ATTRS));
        return location;
    }
}

class LocationWrapperConverter implements CursorConverter<DataWrapper> {
    @Override
    @NotNull
    public DataWrapper convert(@NotNull Cursor c) {
        DataWrapper dataWrapper = new DataWrapper(
                extractString(c, COLUMN_LOCATION_UUID),
                HOUSEHOLD,
                extractString(c, COLUMN_LOCATION_EXTID),
                extractString(c, COLUMN_LOCATION_NAME)
        );
        dataWrapper.getStringsPayload().put(R.string.location_description_label, extractString(c, COLUMN_LOCATION_DESCRIPTION));
        return dataWrapper;
    }
}

class LocationContentValuesConverter implements ContentValuesConverter<Location> {

    @Override
    @NotNull
    public ContentValues toContentValues(@NotNull Location location) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LOCATION_UUID, location.getUuid());
        contentValues.put(COLUMN_LOCATION_EXTID, location.getExtId());
        contentValues.put(COLUMN_LOCATION_HIERARCHY_UUID, location.getHierarchyUuid());
        contentValues.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
        contentValues.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
        contentValues.put(COLUMN_LOCATION_NAME, location.getName());
        contentValues.put(COLUMN_LOCATION_DESCRIPTION, location.getDescription());
        contentValues.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
        contentValues.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
        contentValues.put(COLUMN_LOCATION_ATTRS, location.getAttrs());
        return contentValues;
    }
}
