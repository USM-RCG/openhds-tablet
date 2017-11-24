package org.openhds.mobile.navconfig.forms.adapters;

import org.openhds.mobile.model.core.Location;

import java.util.Map;

import static org.openhds.mobile.OpenHDS.Locations.*;
import static org.openhds.mobile.navconfig.ProjectFormFields.Locations.getFieldNameFromColumn;

public class LocationFormAdapter {

    public static Location fromForm(Map<String, String> formInstanceData) {
        Location location = new Location();
        location.setUuid(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_UUID)));
        location.setExtId(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_EXTID)));
        location.setName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_NAME)));
        location.setHierarchyUuid(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_HIERARCHY_UUID)));
        location.setHierarchyExtId(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_HIERARCHY_EXTID)));
        location.setCommunityName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_COMMUNITY_NAME)));
        location.setCommunityCode(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_COMMUNITY_CODE)));
        location.setLocalityName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_LOCALITY_NAME)));
        location.setSectorName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_SECTOR_NAME)));
        location.setMapAreaName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_MAP_AREA_NAME)));
        location.setBuildingNumber(Integer.parseInt(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_BUILDING_NUMBER))));
        location.setFloorNumber(Integer.parseInt(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_FLOOR_NUMBER))));
        location.setRegionName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_REGION_NAME)));
        location.setProvinceName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_PROVINCE_NAME)));
        location.setSubDistrictName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_SUB_DISTRICT_NAME)));
        location.setDistrictName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_DISTRICT_NAME)));
        location.setDescription(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_DESCRIPTION)));
        location.setLongitude(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_LONGITUDE)));
        location.setLatitude(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_LATITUDE)));
        return location;
    }
}
