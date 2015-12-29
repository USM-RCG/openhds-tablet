package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;


public class BiokoFormFilters {

    public static class DistributeBednets implements FormFilter {

        @Override
        public boolean amIValid(NavigateActivity navigateActivity) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(navigateActivity.getCurrentSelection().getUuid()));

            if (!"true".equals(location.getHasReceivedBedNets())) {
                return true;
            }
            return false;
        }

    }

    public static class SprayHousehold implements FormFilter {

        @Override
        public boolean amIValid(NavigateActivity navigateActivity) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(navigateActivity.getCurrentSelection().getUuid()));

            if (location.getSprayingEvaluation() == null) {
                return true;
            }
            return false;
        }
    }

    public static class SuperOjo implements FormFilter {

        @Override
        public boolean amIValid(NavigateActivity navigateActivity) {
            return true;
        }
    }

}
