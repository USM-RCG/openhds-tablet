package org.cimsbioko.data;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Well known location for accessing type-specific gateway instances.
 */
public class GatewayRegistry {

    private static final String TAG = GatewayRegistry.class.getSimpleName();

    private static final Map<String, Gateway> SINGLETONS;

    static {
        SINGLETONS = new HashMap<>();
    }

    private GatewayRegistry() {
    }

    /**
     * Creates lazy-loaded singletons by creating on first access and then
     * returning the same value for subsequent access.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Gateway> T lazy(Class<T> gatewayClass) {
        final String gatewayName = gatewayClass.getName();
        if (!SINGLETONS.containsKey(gatewayName)) {
            try {
                SINGLETONS.put(gatewayName, gatewayClass.newInstance());
            } catch (Exception e) {
                Log.w(TAG, "failed to create gateway " + gatewayName, e);
            }
        }
        return (T) SINGLETONS.get(gatewayName);
    }

    public static FieldWorkerGateway getFieldWorkerGateway() {
        return lazy(FieldWorkerGateway.class);
    }

    public static IndividualGateway getIndividualGateway() {
        return lazy(IndividualGateway.class);
    }

    public static LocationGateway getLocationGateway() {
        return lazy(LocationGateway.class);
    }

    public static LocationHierarchyGateway getLocationHierarchyGateway() {
        return lazy(LocationHierarchyGateway.class);
    }
}
