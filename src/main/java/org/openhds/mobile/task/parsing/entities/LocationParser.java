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
        location.setHierarchyUuid(dataPage.getFirstString(asList(pageName, "hierUuid")));
        location.setHierarchyExtId(dataPage.getFirstString(asList(pageName, "hierExtId")));
        location.setName(dataPage.getFirstString(asList(pageName, "name")));
        location.setDescription(dataPage.getFirstString(asList(pageName, "description")));
        location.setCommunityName(dataPage.getFirstString(asList(pageName, "community")));
        location.setCommunityCode       (dataPage.getFirstString(asList(pageName, "communityCode")));
        location.setLocalityName(dataPage.getFirstString(asList(pageName, "locality")));
        location.setMapAreaName(dataPage.getFirstString(asList(pageName, "map")));
        location.setSectorName(dataPage.getFirstString(asList(pageName, "sector")));
        location.setBuildingNumber(dataPage.getFirstInt(asList(pageName, "building")));
        location.setFloorNumber(dataPage.getFirstInt(asList(pageName, "floor")));
        location.setLatitude(dataPage.getFirstString(asList(pageName, "latitude")));
        location.setLongitude(dataPage.getFirstString(asList(pageName, "longitude")));
        return location;
    }
}
