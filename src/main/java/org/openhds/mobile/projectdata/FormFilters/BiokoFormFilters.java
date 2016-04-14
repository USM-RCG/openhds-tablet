package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.activity.HierarchyNavigatorActivity;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;


public class BiokoFormFilters {

    public static class DistributeBednets implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(navigateActivity.getCurrentSelection().getUuid()));

            return !"true".equals(location.getHasReceivedBedNets());
        }

    }

    public static class SprayHousehold implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(navigateActivity.getCurrentSelection().getUuid()));

            return location.getSprayingEvaluation() == null;
        }
    }

    public static class SuperOjo implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {
            return true;
        }
    }

    public static class DuplicateLocation implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {
            return true;
        }
    }

}
