package org.cimsbioko.navconfig.forms.adapters;

import org.cimsbioko.model.core.Location;

import java.util.Map;

import static org.cimsbioko.navconfig.ProjectFormFields.General.ENTITY_UUID;

public class LocationFormAdapter {

    public static Location fromForm(Map<String, String> data) {
        Location l = new Location();
        l.setUuid(data.get(ENTITY_UUID));
        l.setExtId(data.get("locationExtId"));
        l.setName(data.get("locationName"));
        l.setHierarchyUuid(data.get("hierarchyUuid"));
        l.setSectorName(data.get("sectorName"));
        l.setMapAreaName(data.get("mapAreaName"));
        l.setBuildingNumber(Integer.parseInt(data.get("locationBuildingNumber")));
        l.setDescription(data.get("description"));
        l.setLongitude(data.get("longitude"));
        l.setLatitude(data.get("latitude"));
        return l;
    }
}
