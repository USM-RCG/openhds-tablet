package org.openhds.mobile.navconfig.forms.filters;

import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;

import static org.openhds.mobile.navconfig.BiokoHierarchy.HOUSEHOLD;


public class CensusFormFilters {

    public static class AddLocation implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return true;
        }
    }

    public static class EvaluateLocation implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Location location = locationGateway.getFirst(ctx.getContentResolver(),
                    locationGateway.findById(ctx.getCurrentSelection().getUuid()));
            return location.getLocationEvaluationStatus() == null;
        }
    }

    public static class AddHeadOfHousehold implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            String locationUuid = ctx.getHierarchyPath().get(HOUSEHOLD).getUuid();
            SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
            return socialGroupGateway.getFirst(ctx.getContentResolver(),
                    socialGroupGateway.findByLocationUuid(locationUuid)) == null;
        }
    }
}
