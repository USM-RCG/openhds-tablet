package org.cimsbioko.repository.gateway;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.cimsbioko.App;
import org.cimsbioko.R;
import org.cimsbioko.model.core.Location;
import org.cimsbioko.provider.ContentProvider;
import org.cimsbioko.repository.ContentValuesConverter;
import org.cimsbioko.repository.CursorConverter;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.repository.Query;

import java.util.HashMap;
import java.util.Map;

import static org.cimsbioko.App.Locations.COLUMN_LOCATION_ATTRS;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_BUILDING_NUMBER;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_DESCRIPTION;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_EXTID;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_HIERARCHY_UUID;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_LATITUDE;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_LONGITUDE;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_MAP_AREA_NAME;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_NAME;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_SECTOR_NAME;
import static org.cimsbioko.App.Locations.COLUMN_LOCATION_UUID;
import static org.cimsbioko.App.Locations.TABLE_NAME;
import static org.cimsbioko.repository.RepositoryUtils.extractInt;
import static org.cimsbioko.repository.RepositoryUtils.extractString;

/**
 * Convert Locations to and from database.  Location-specific queries.
 */
public class LocationGateway extends Gateway<Location> {

    private static final LocationEntityConverter ENTITY_CONVERTER = new LocationEntityConverter();
    private static final Map<String, LocationWrapperConverter> WRAPPER_CONVERTERS = new HashMap<>();
    private static final LocationContentValuesConverter CONTENT_VALUES_CONVERTER = new LocationContentValuesConverter();

    public LocationGateway() {
        super(App.Locations.CONTENT_ID_URI_BASE, COLUMN_LOCATION_UUID);
    }

    public Query<Location> findByHierarchy(String hierarchyId) {
        return new Query<>(this, tableUri, COLUMN_LOCATION_HIERARCHY_UUID, hierarchyId, COLUMN_LOCATION_EXTID);
    }

    /**
     * Calculates the next sequential building number for the given sector. This considers all locations with the same
     * map and sector name and not just locations referencing the same parent sector node. This is necessary since
     * multiple sector nodes are created when a sector spans multiple localities.
     *
     * @param mapArea map name for sector
     * @param sector sector name
     * @return the next sequential building number to use for a new location in the given sector
     */
    public int nextBuildingNumberInSector(String mapArea, String sector) {
        SQLiteDatabase db = ContentProvider.getDatabaseHelper(App.getApp().getApplicationContext()).getReadableDatabase();
        String query = String.format("select max(%s) + 1 from %s where %s = ? and %s = ?",
                COLUMN_LOCATION_BUILDING_NUMBER, TABLE_NAME, COLUMN_LOCATION_MAP_AREA_NAME, COLUMN_LOCATION_SECTOR_NAME);
        String[] args = {mapArea, sector};
        Cursor c = db.rawQuery(query, args);
        try {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 1;
        } finally {
            c.close();
        }
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
    public CursorConverter<DataWrapper> getWrapperConverter(String level) {
        if (WRAPPER_CONVERTERS.containsKey(level)) {
            return WRAPPER_CONVERTERS.get(level);
        } else {
            LocationWrapperConverter converter = new LocationWrapperConverter(level);
            WRAPPER_CONVERTERS.put(level, converter);
            return converter;
        }
    }

    @Override
    ContentValuesConverter<Location> getContentValuesConverter() {
        return CONTENT_VALUES_CONVERTER;
    }
}

class LocationEntityConverter implements CursorConverter<Location> {
    @Override
    public Location convert(Cursor c) {
        Location location = new Location();
        location.setUuid(extractString(c, COLUMN_LOCATION_UUID));
        location.setExtId(extractString(c, COLUMN_LOCATION_EXTID));
        location.setHierarchyUuid(extractString(c, COLUMN_LOCATION_HIERARCHY_UUID));
        location.setLatitude(extractString(c, COLUMN_LOCATION_LATITUDE));
        location.setLongitude(extractString(c, COLUMN_LOCATION_LONGITUDE));
        location.setName(extractString(c, COLUMN_LOCATION_NAME));
        location.setSectorName(extractString(c, COLUMN_LOCATION_SECTOR_NAME));
        location.setMapAreaName(extractString(c, COLUMN_LOCATION_MAP_AREA_NAME));
        location.setBuildingNumber(extractInt(c, COLUMN_LOCATION_BUILDING_NUMBER));
        location.setDescription(extractString(c, COLUMN_LOCATION_DESCRIPTION));
        location.setLongitude(extractString(c, COLUMN_LOCATION_LONGITUDE));
        location.setLatitude(extractString(c, COLUMN_LOCATION_LATITUDE));
        location.setAttrs(extractString(c, COLUMN_LOCATION_ATTRS));
        return location;
    }
}

class LocationWrapperConverter implements CursorConverter<DataWrapper> {

    private final String level;

    public LocationWrapperConverter(String level) {
        this.level = level;
    }

    @Override
    public DataWrapper convert(Cursor c) {
        DataWrapper dataWrapper = new DataWrapper();
        dataWrapper.setUuid(extractString(c, COLUMN_LOCATION_UUID));
        dataWrapper.setExtId(extractString(c, COLUMN_LOCATION_EXTID));
        dataWrapper.setName(extractString(c, COLUMN_LOCATION_NAME));
        dataWrapper.getStringsPayload().put(R.string.location_description_label, extractString(c, COLUMN_LOCATION_DESCRIPTION));
        dataWrapper.setCategory(level);
        return dataWrapper;
    }
}

class LocationContentValuesConverter implements ContentValuesConverter<Location> {

    @Override
    public ContentValues toContentValues(Location location) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LOCATION_UUID, location.getUuid());
        contentValues.put(COLUMN_LOCATION_EXTID, location.getExtId());
        contentValues.put(COLUMN_LOCATION_HIERARCHY_UUID, location.getHierarchyUuid());
        contentValues.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
        contentValues.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
        contentValues.put(COLUMN_LOCATION_NAME, location.getName());
        contentValues.put(COLUMN_LOCATION_SECTOR_NAME, location.getSectorName());
        contentValues.put(COLUMN_LOCATION_MAP_AREA_NAME, location.getMapAreaName());
        contentValues.put(COLUMN_LOCATION_BUILDING_NUMBER, location.getBuildingNumber());
        contentValues.put(COLUMN_LOCATION_DESCRIPTION, location.getDescription());
        contentValues.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
        contentValues.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
        contentValues.put(COLUMN_LOCATION_ATTRS, location.getAttrs());
        return contentValues;
    }
}
