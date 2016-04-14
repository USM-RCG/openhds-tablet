package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.activity.HierarchyNavigatorActivity;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;

import java.util.Map;

import static org.openhds.mobile.projectdata.BiokoHierarchy.*;

// These are not necessarily 1 to 1 with the form types, 
// but instead filter when a form's behaviour may or may not be appropriate
// i.e. after you 'add a head of household' you no longer have to display the
// button (aka it's amIValid() == false).
public class CensusFormFilters {

	private static boolean hasHeadOfHousehold(
			HierarchyNavigatorActivity navigateActivity,
			Map<String, DataWrapper> hierarchyPath) {

		String locationUuid = hierarchyPath.get(HOUSEHOLD_STATE).getUuid();

        SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
        return null != socialGroupGateway.getFirst(navigateActivity.getContentResolver(),socialGroupGateway.findByLocationUuid(locationUuid));
	}

    public static class AddLocation implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

            return true;
        }
    }

    public static class EvaluateLocation implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(navigateActivity.getCurrentSelection().getUuid()));

			return location.getLocationEvaluationStatus() == null;
		}
    }

    public static class AddHeadOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

			return !CensusFormFilters.hasHeadOfHousehold(navigateActivity,
					navigateActivity.getHierarchyPath());
		}
	}

	public static class AddMemberOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

			return CensusFormFilters.hasHeadOfHousehold(navigateActivity,
					navigateActivity.getHierarchyPath());
		}
	}

	public static class EditIndividual implements FormFilter {

		@Override
		public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

			return true;
		}
	}
}
