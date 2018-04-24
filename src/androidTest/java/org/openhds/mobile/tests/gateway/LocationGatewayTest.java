package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.repository.gateway.LocationGateway;

public class LocationGatewayTest extends GatewayTest<Location> {

    public LocationGatewayTest() {
        super(new LocationGateway());
    }

    @Override
    protected Location makeTestEntity(String id, String name) {
        Location location = new Location();

        location.setUuid(id);
        location.setExtId(id);
        location.setName(name);
        location.setLongitude("1234.5678");
        location.setLatitude("9876.5432");
        location.setHierarchyUuid("HIERARCHY");

        return location;
    }
}
