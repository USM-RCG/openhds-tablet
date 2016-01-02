package org.openhds.mobile.repository;

import android.util.Log;

import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;

import java.util.HashMap;
import java.util.Map;

/**
 * Well known location for accessing type-specific gateway instances.
 */
public class GatewayRegistry {

    private static final String TAG = GatewayRegistry.class.getName();

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

    public static MembershipGateway getMembershipGateway() {
        return lazy(MembershipGateway.class);
    }

    public static RelationshipGateway getRelationshipGateway() {
        return lazy(RelationshipGateway.class);
    }

    public static SocialGroupGateway getSocialGroupGateway() {
        return lazy(SocialGroupGateway.class);
    }

    public static VisitGateway getVisitGateway() {
        return lazy(VisitGateway.class);
    }

    @SuppressWarnings("unchecked")
    public static Gateway getGatewayByName(String name) {
        try {
            return lazy((Class<Gateway>) Class.forName(name));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("failed to load gateway class " + name);
        }
    }
}
