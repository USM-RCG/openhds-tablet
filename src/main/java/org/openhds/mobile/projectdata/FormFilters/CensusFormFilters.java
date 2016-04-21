package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.projectdata.FormPayloadBuilders.LaunchContext;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;

import java.util.Map;

import static org.openhds.mobile.projectdata.BiokoHierarchy.HOUSEHOLD_STATE;


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

    private static boolean hasHeadOfHousehold(LaunchContext ctx, Map<String, DataWrapper> hierarchyPath) {
        String locationUuid = hierarchyPath.get(HOUSEHOLD_STATE).getUuid();
        SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
        return socialGroupGateway.getFirst(ctx.getContentResolver(),
                socialGroupGateway.findByLocationUuid(locationUuid)) != null;
    }

    public static class AddHeadOfHousehold implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return !hasHeadOfHousehold(ctx, ctx.getHierarchyPath());
        }
    }

    public static class AddMemberOfHousehold implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return hasHeadOfHousehold(ctx, ctx.getHierarchyPath());
        }
    }
}
