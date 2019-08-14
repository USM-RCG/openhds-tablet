package org.cimsbioko.navconfig.forms.adapters;

import org.cimsbioko.model.core.Location;

import java.util.Map;

import static org.cimsbioko.App.Locations.*;
import static org.cimsbioko.navconfig.ProjectFormFields.Locations.getFieldNameFromColumn;

public class LocationFormAdapter {

    public static Location fromForm(Map<String, String> formInstanceData) {
        Location location = new Location();
        location.setUuid(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_UUID)));
        location.setExtId(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_EXTID)));
        location.setName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_NAME)));
        location.setHierarchyUuid(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_HIERARCHY_UUID)));
        location.setSectorName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_SECTOR_NAME)));
        location.setMapAreaName(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_MAP_AREA_NAME)));
        location.setBuildingNumber(Integer.parseInt(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_BUILDING_NUMBER))));
        location.setDescription(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_DESCRIPTION)));
        location.setLongitude(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_LONGITUDE)));
        location.setLatitude(formInstanceData.get(getFieldNameFromColumn(COLUMN_LOCATION_LATITUDE)));
        return location;
    }
}
