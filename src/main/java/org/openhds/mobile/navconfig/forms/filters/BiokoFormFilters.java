package org.openhds.mobile.navconfig.forms.filters;

import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.navconfig.forms.builders.LaunchContext;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;


public class BiokoFormFilters {

    public static class DistributeBednets implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return !"true".equals(getLocation(ctx).getHasReceivedBedNets());
        }
    }

    public static class SprayHousehold implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return getLocation(ctx).getSprayingEvaluation() == null;
        }
    }

    private static Location getLocation(LaunchContext ctx) {
        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
        return locationGateway.getFirst(ctx.getContentResolver(),
                locationGateway.findById(ctx.getCurrentSelection().getUuid()));
    }

}
