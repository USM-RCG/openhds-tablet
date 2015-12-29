package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Locations.
 */
public class LocationParser extends EntityParser<Location> {

    private static final String pageName = "location";

    @Override
    protected Location toEntity(DataPage dataPage) {
        Location location = new Location();

        location.setUuid(dataPage.getFirstString(asList(pageName, "uuid")));
        location.setExtId(dataPage.getFirstString(asList(pageName, "extId")));
        location.setHierarchyUuid(dataPage.getFirstString(asList(pageName, "locationHierarchy", "uuid")));
        location.setHierarchyExtId(dataPage.getFirstString(asList(pageName, "locationHierarchy", "extId")));
        location.setLatitude(dataPage.getFirstString(asList(pageName, "latitude")));
        location.setLongitude(dataPage.getFirstString(asList(pageName, "longitude")));
        location.setName(dataPage.getFirstString(asList(pageName, "locationName")));
        location.setSectorName(dataPage.getFirstString(asList(pageName, "sectorName")));
        location.setMapAreaName(dataPage.getFirstString(asList(pageName, "mapAreaName")));
        location.setLocalityName(dataPage.getFirstString(asList(pageName, "localityName")));
        location.setCommunityName(dataPage.getFirstString(asList(pageName, "communityName")));
        location.setCommunityCode(dataPage.getFirstString(asList(pageName, "communityCode")));
        location.setBuildingNumber(dataPage.getFirstInt(asList(pageName, "buildingNumber")));
        location.setFloorNumber(dataPage.getFirstInt(asList(pageName, "floorNumber")));
        location.setDescription(dataPage.getFirstString(asList(pageName, "description")));
        location.setLocationEvaluationStatus(dataPage.getFirstString(asList(pageName, "evaluationStatus")));
        location.setLongitude(dataPage.getFirstString(asList(pageName, "longitude")));
        location.setLatitude(dataPage.getFirstString(asList(pageName, "latitude")));


        return location;
    }
}
