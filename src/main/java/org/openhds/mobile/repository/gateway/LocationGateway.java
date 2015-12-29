package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_CODE;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_DESCRIPTION;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_EXTID;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_FLOOR_NUMBER;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_HAS_RECIEVED_BEDNETS;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_SPRAYING_EVALUATION;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_EXTID;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_UUID;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LATITUDE;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_EVALUATION_STATUS;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_UUID;
import static org.openhds.mobile.repository.RepositoryUtils.extractInt;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;

/**
 * Convert Locations to and from database.  Location-specific queries.
 */
public class LocationGateway extends Gateway<Location> {

    public LocationGateway() {
        super(OpenHDS.Locations.CONTENT_ID_URI_BASE, COLUMN_LOCATION_UUID, new LocationConverter());
    }

    public Query findByHierarchy(String hierarchyId) {
        return new Query(tableUri, COLUMN_LOCATION_HIERARCHY_UUID, hierarchyId, COLUMN_LOCATION_UUID);
    }

    // for Bioko
    public Query findByHierarchyDescendingBuildingNumber(String hierarchyId) {
        return new Query(tableUri, COLUMN_LOCATION_HIERARCHY_UUID, hierarchyId, COLUMN_LOCATION_BUILDING_NUMBER + " DESC");
    }

    private static class LocationConverter implements Converter<Location> {

        @Override
        public Location fromCursor(Cursor cursor) {
            Location location = new Location();

            location.setUuid(extractString(cursor, COLUMN_LOCATION_UUID));
            location.setExtId(extractString(cursor, COLUMN_LOCATION_EXTID));
            location.setHierarchyUuid(extractString(cursor, COLUMN_LOCATION_HIERARCHY_UUID));
            location.setHierarchyExtId(extractString(cursor, COLUMN_LOCATION_HIERARCHY_EXTID));
            location.setLatitude(extractString(cursor, COLUMN_LOCATION_LATITUDE));
            location.setLongitude(extractString(cursor, COLUMN_LOCATION_LONGITUDE));
            location.setName(extractString(cursor, COLUMN_LOCATION_NAME));
            location.setSectorName(extractString(cursor, COLUMN_LOCATION_SECTOR_NAME));
            location.setMapAreaName(extractString(cursor, COLUMN_LOCATION_MAP_AREA_NAME));
            location.setLocalityName(extractString(cursor, COLUMN_LOCATION_LOCALITY_NAME));
            location.setCommunityName(extractString(cursor, COLUMN_LOCATION_COMMUNITY_NAME));
            location.setCommunityCode(extractString(cursor, COLUMN_LOCATION_COMMUNITY_CODE));
            location.setBuildingNumber(extractInt(cursor, COLUMN_LOCATION_BUILDING_NUMBER));
            location.setFloorNumber(extractInt(cursor, COLUMN_LOCATION_FLOOR_NUMBER));
            location.setHasReceivedBedNets(extractString(cursor, COLUMN_LOCATION_HAS_RECIEVED_BEDNETS));
            location.setSprayingEvaluation(extractString(cursor, COLUMN_LOCATION_SPRAYING_EVALUATION));
            location.setDescription(extractString(cursor, COLUMN_LOCATION_DESCRIPTION));
            location.setLocationEvaluationStatus(extractString(cursor, COLUMN_LOCATION_EVALUATION_STATUS));
            location.setLongitude(extractString(cursor, COLUMN_LOCATION_LONGITUDE));
            location.setLatitude(extractString(cursor, COLUMN_LOCATION_LATITUDE));


            return location;
        }

        @Override
        public ContentValues toContentValues(Location location) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_LOCATION_UUID, location.getUuid());
            contentValues.put(COLUMN_LOCATION_EXTID, location.getExtId());
            contentValues.put(COLUMN_LOCATION_HIERARCHY_UUID, location.getHierarchyUuid());
            contentValues.put(COLUMN_LOCATION_HIERARCHY_EXTID, location.getHierarchyExtId());
            contentValues.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
            contentValues.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
            contentValues.put(COLUMN_LOCATION_NAME, location.getName());
            contentValues.put(COLUMN_LOCATION_SECTOR_NAME, location.getSectorName());
            contentValues.put(COLUMN_LOCATION_MAP_AREA_NAME, location.getMapAreaName());
            contentValues.put(COLUMN_LOCATION_LOCALITY_NAME, location.getLocalityName());
            contentValues.put(COLUMN_LOCATION_COMMUNITY_NAME, location.getCommunityName());
            contentValues.put(COLUMN_LOCATION_COMMUNITY_CODE, location.getCommunityCode());
            contentValues.put(COLUMN_LOCATION_BUILDING_NUMBER, location.getBuildingNumber());
            contentValues.put(COLUMN_LOCATION_FLOOR_NUMBER, location.getFloorNumber());
            contentValues.put(COLUMN_LOCATION_HAS_RECIEVED_BEDNETS, location.getHasReceivedBedNets());
            contentValues.put(COLUMN_LOCATION_SPRAYING_EVALUATION, location.getSprayingEvaluation());
            contentValues.put(COLUMN_LOCATION_DESCRIPTION, location.getDescription());
            contentValues.put(COLUMN_LOCATION_EVALUATION_STATUS, location.getLocationEvaluationStatus());
            contentValues.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
            contentValues.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());

            return contentValues;
        }

        @Override
        public String getId(Location location) {
            return location.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Location location, String state) {

            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setUuid(location.getUuid());
            dataWrapper.setExtId(location.getExtId());
            dataWrapper.setName(location.getName());
            dataWrapper.getStringsPayload().put(R.string.location_description_label, location.getDescription());

            if(null != location.getHasReceivedBedNets()) {
                dataWrapper.getStringIdsPayload().put(R.string.location_has_recieved_bednets_label, ProjectResources.General.getGeneralStringId(location.getHasReceivedBedNets()));
            }

            if (null != location.getSprayingEvaluation()) {
                dataWrapper.getStringsPayload().put(R.string.location_spraying_status_label, location.getSprayingEvaluation());
            }

            if(null != location.getLocationEvaluationStatus()) {
                String[] statusValues = location.getLocationEvaluationStatus().split(" ");
                for (String value : statusValues) {
                    Integer payloadKey = ProjectResources.Location.getLocationStringId(value);
                    if (0 == payloadKey) {
                        continue;
                    }
                    dataWrapper.getStringIdsPayload().put(payloadKey, R.string.db_val_true);
                }
            }

            dataWrapper.setCategory(state);
            return dataWrapper;
        }
    }
}
