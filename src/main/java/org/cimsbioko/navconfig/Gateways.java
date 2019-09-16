package org.cimsbioko.navconfig;

import org.cimsbioko.data.FieldWorkerGateway;
import org.cimsbioko.data.IndividualGateway;
import org.cimsbioko.data.LocationGateway;
import org.cimsbioko.data.LocationHierarchyGateway;

import static org.cimsbioko.data.GatewayRegistry.*;

/**
 * Used to provide scripts an interface into the application gateways
 */
public final class Gateways {

    private static Gateways instance;

    /**
     * Prevent direct instantiation, should use singleton.
     */
    private Gateways() {
    }

    public static Gateways getInstance() {
        if (instance == null) {
            instance = new Gateways();
        }
        return instance;
    }

    @UsedByJSConfig
    public IndividualGateway getIndividuals() {
        return getIndividualGateway();
    }

    @UsedByJSConfig
    public LocationGateway getLocations() {
        return getLocationGateway();
    }

    @UsedByJSConfig
    public LocationHierarchyGateway getHierarchy() {
        return getLocationHierarchyGateway();
    }

    @UsedByJSConfig
    public FieldWorkerGateway getFieldworkers() {
        return getFieldWorkerGateway();
    }
}
