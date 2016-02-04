package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to LocationHierarchies.
 */
public class LocationHierarchyParser extends EntityParser<LocationHierarchy> {

    private static final String pageName = "hierarchy";

    @Override
    protected LocationHierarchy toEntity(DataPage dataPage) {
        LocationHierarchy locationHierarchy = new LocationHierarchy();
        locationHierarchy.setUuid(dataPage.getFirstString(asList(pageName, "uuid")));
        locationHierarchy.setExtId(dataPage.getFirstString(asList(pageName, "extId")));
        locationHierarchy.setName(dataPage.getFirstString(asList(pageName, "name")));
        locationHierarchy.setLevel(dataPage.getFirstString(asList(pageName, "level")));
        locationHierarchy.setParentUuid(dataPage.getFirstString(asList(pageName, "parent")));
        return locationHierarchy;
    }
}
