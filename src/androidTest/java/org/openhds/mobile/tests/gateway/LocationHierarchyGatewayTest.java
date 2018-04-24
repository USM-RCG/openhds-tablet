package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;

public class LocationHierarchyGatewayTest extends GatewayTest<LocationHierarchy> {

    public LocationHierarchyGatewayTest() {
        super(new LocationHierarchyGateway());
    }

    @Override
    protected LocationHierarchy makeTestEntity(String id, String name) {
        LocationHierarchy locationHierarchy = new LocationHierarchy();

        locationHierarchy.setUuid(id);
        locationHierarchy.setExtId(id);
        locationHierarchy.setName(name);
        locationHierarchy.setLevel("HIERARCHY");
        locationHierarchy.setParentUuid("PARENT");

        return locationHierarchy;
    }
}
