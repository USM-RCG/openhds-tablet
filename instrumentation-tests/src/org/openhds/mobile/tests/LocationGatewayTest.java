package org.openhds.mobile.tests;

import org.openhds.mobile.model.Location;
import org.openhds.mobile.repository.LocationGateway;


public class LocationGatewayTest extends GatewayTest<Location> {

    private LocationGateway locationGateway;

    public LocationGatewayTest() {
        super(new LocationGateway());
        this.locationGateway = (LocationGateway) this.gateway;
    }

    @Override
    protected Location makeTestEntity(String id, String name) {
        Location location = new Location();

        location.setExtId(id);
        location.setName(name);
        location.setLongitude("1234.5678");
        location.setLatitude("9876.5432");
        location.setHierarchyExtId("HIERARCHY");

        return location;
    }
}