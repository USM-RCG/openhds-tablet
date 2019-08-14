package org.cimsbioko.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.cimsbioko.App;
import org.cimsbioko.R;
import org.cimsbioko.model.core.Location;
import org.cimsbioko.provider.ContentProvider;
import org.cimsbioko.repository.Converter;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.repository.Query;

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

    public LocationGateway() {
        super(App.Locations.CONTENT_ID_URI_BASE, COLUMN_LOCATION_UUID, new LocationConverter());
    }

    public Query findByHierarchy(String hierarchyId) {
        return new Query(tableUri, COLUMN_LOCATION_HIERARCHY_UUID, hierarchyId, COLUMN_LOCATION_EXTID);
    }

    /**
     * Calculates the next sequential building number for the given sector. This considers all locations with the same
     * map and sector name and not just locations referencing the same parent sector node. This is necessary since
     * multiple sector nodes are created when a sector spans multiple localities.
     *
     * @param ctx used to lookup database for direct query
     * @param mapArea map name for sector
     * @param sector sector name
     * @return the next sequential building number to use for a new location in the given sector
     */
    public int nextBuildingNumberInSector(Context ctx, String mapArea, String sector) {
        SQLiteDatabase db = ContentProvider.getDatabaseHelper(ctx).getReadableDatabase();
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

    private static class LocationConverter implements Converter<Location> {

        @Override
        public Location fromCursor(Cursor cursor) {
            Location location = new Location();
            location.setUuid(extractString(cursor, COLUMN_LOCATION_UUID));
            location.setExtId(extractString(cursor, COLUMN_LOCATION_EXTID));
            location.setHierarchyUuid(extractString(cursor, COLUMN_LOCATION_HIERARCHY_UUID));
            location.setLatitude(extractString(cursor, COLUMN_LOCATION_LATITUDE));
            location.setLongitude(extractString(cursor, COLUMN_LOCATION_LONGITUDE));
            location.setName(extractString(cursor, COLUMN_LOCATION_NAME));
            location.setSectorName(extractString(cursor, COLUMN_LOCATION_SECTOR_NAME));
            location.setMapAreaName(extractString(cursor, COLUMN_LOCATION_MAP_AREA_NAME));
            location.setBuildingNumber(extractInt(cursor, COLUMN_LOCATION_BUILDING_NUMBER));
            location.setDescription(extractString(cursor, COLUMN_LOCATION_DESCRIPTION));
            location.setLongitude(extractString(cursor, COLUMN_LOCATION_LONGITUDE));
            location.setLatitude(extractString(cursor, COLUMN_LOCATION_LATITUDE));
            location.setAttrs(extractString(cursor, COLUMN_LOCATION_ATTRS));
            return location;
        }

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

        @Override
        public String getId(Location location) {
            return location.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Location location, String level) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setUuid(location.getUuid());
            dataWrapper.setExtId(location.getExtId());
            dataWrapper.setName(location.getName());
            dataWrapper.getStringsPayload().put(R.string.location_description_label, location.getDescription());
            dataWrapper.setCategory(level);
            return dataWrapper;
        }
    }
}
