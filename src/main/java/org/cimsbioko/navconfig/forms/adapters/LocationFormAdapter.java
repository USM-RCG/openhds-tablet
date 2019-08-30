package org.cimsbioko.navconfig.forms.adapters;

import org.cimsbioko.model.core.Location;

import java.util.Map;

import static org.cimsbioko.App.Locations.*;
import static org.cimsbioko.navconfig.ProjectFormFields.Locations.getFieldNameFromColumn;

public class LocationFormAdapter {

    public static Location fromForm(Map<String, String> data) {
        Location l = new Location();
        l.setUuid(data.get(getFieldNameFromColumn(COLUMN_LOCATION_UUID)));
        l.setExtId(data.get(getFieldNameFromColumn(COLUMN_LOCATION_EXTID)));
        l.setName(data.get(getFieldNameFromColumn(COLUMN_LOCATION_NAME)));
        l.setHierarchyUuid(data.get(getFieldNameFromColumn(COLUMN_LOCATION_HIERARCHY_UUID)));
        l.setSectorName(data.get(getFieldNameFromColumn(COLUMN_LOCATION_SECTOR_NAME)));
        l.setMapAreaName(data.get(getFieldNameFromColumn(COLUMN_LOCATION_MAP_AREA_NAME)));
        l.setBuildingNumber(Integer.parseInt(data.get(getFieldNameFromColumn(COLUMN_LOCATION_BUILDING_NUMBER))));
        l.setDescription(data.get(getFieldNameFromColumn(COLUMN_LOCATION_DESCRIPTION)));
        l.setLongitude(data.get(getFieldNameFromColumn(COLUMN_LOCATION_LONGITUDE)));
        l.setLatitude(data.get(getFieldNameFromColumn(COLUMN_LOCATION_LATITUDE)));
        return l;
    }
}
