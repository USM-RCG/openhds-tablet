package org.cimsbioko.navconfig.forms.adapters;

import org.cimsbioko.model.core.Location;

import java.util.Map;

import static org.cimsbioko.navconfig.ProjectFormFields.General.ENTITY_UUID;
import static org.cimsbioko.navconfig.ProjectFormFields.Locations.*;

public class LocationFormAdapter {

    public static Location fromForm(Map<String, String> data) {
        Location l = new Location();
        l.setUuid(data.get(ENTITY_UUID));
        l.setExtId(data.get(LOCATION_EXTID));
        l.setName(data.get(LOCATION_NAME));
        l.setHierarchyUuid(data.get(HIERARCHY_UUID));
        l.setSectorName(data.get(SECTOR_NAME));
        l.setMapAreaName(data.get(MAP_AREA_NAME));
        l.setBuildingNumber(Integer.parseInt(data.get(BUILDING_NUMBER)));
        l.setDescription(data.get(DESCRIPTION));
        l.setLongitude(data.get(LONGITUDE));
        l.setLatitude(data.get(LATITUDE));
        return l;
    }
}
