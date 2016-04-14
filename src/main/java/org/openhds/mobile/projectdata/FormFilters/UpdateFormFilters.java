package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.activity.HierarchyNavigatorActivity;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import static org.openhds.mobile.projectdata.BiokoHierarchy.*;

import java.util.Map;

// These are not necessarily 1 to 1 with the form types, 
// but instead filter when a form's behaviour may or may not be appropriate
// i.e. after you 'add a head of household' you no longer have to display the
// button (aka it's amIValid() == false).
public class UpdateFormFilters {

    public static class StartAVisit implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {
            return navigateActivity.getCurrentVisit() == null;
        }
    }

    public static class RegisterInMigration implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

            if (null == navigateActivity.getCurrentVisit()) {
                return false;
            }

            String locationKey = HOUSEHOLD_STATE;
            Map<String, DataWrapper> hierarchyPath = navigateActivity.getHierarchyPath();
            return hierarchyPath.containsKey(locationKey) && hierarchyPath.get(locationKey) != null;

        }
    }

    public static class RegisterOutMigration implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

            Individual selectedIndividual = getCurrentSelectedIndividual(navigateActivity);

            boolean deceased = UpdateFormFilters.isIndividualDeceased(selectedIndividual);
            boolean isOutMigrated = UpdateFormFilters.isIndividualOutMigrated(selectedIndividual);

            return navigateActivity.getCurrentVisit() != null && !deceased && !isOutMigrated;

        }
    }

    public static class RegisterDeath implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

            Individual selectedIndividual = getCurrentSelectedIndividual(navigateActivity);

            boolean deceased = UpdateFormFilters.isIndividualDeceased(selectedIndividual);
            boolean isOutMigrated = UpdateFormFilters.isIndividualOutMigrated(selectedIndividual);

            return navigateActivity.getCurrentVisit() != null && !deceased && !isOutMigrated;

        }
    }

    public static class RecordPregnancyObservation implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

            Individual selectedIndividual = getCurrentSelectedIndividual(navigateActivity);

            boolean isDeceased = UpdateFormFilters.isIndividualDeceased(selectedIndividual);
            boolean isFemale = UpdateFormFilters.isIndividualFemale(selectedIndividual);
            boolean isOutMigrated = UpdateFormFilters.isIndividualOutMigrated(selectedIndividual);

            return navigateActivity.getCurrentVisit() != null && isFemale && !isDeceased && !isOutMigrated;

        }
    }

    public static class RecordPregnancyOutcome implements FormFilter {

        @Override
        public boolean amIValid(HierarchyNavigatorActivity navigateActivity) {

            Individual selectedIndividual = getCurrentSelectedIndividual(navigateActivity);

            boolean isDeceased = UpdateFormFilters.isIndividualDeceased(selectedIndividual);
            boolean isFemale = UpdateFormFilters.isIndividualFemale(selectedIndividual);
            boolean isOutMigrated = UpdateFormFilters.isIndividualOutMigrated(selectedIndividual);

            return navigateActivity.getCurrentVisit() != null && isFemale && !isDeceased && !isOutMigrated;

        }
    }

    private static Individual getCurrentSelectedIndividual(HierarchyNavigatorActivity navigateActivity) {

        Map<String, DataWrapper> hierarchyPath = navigateActivity.getHierarchyPath();
        String individualExtId =
                hierarchyPath.get(INDIVIDUAL_STATE).getExtId();
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();

        return individualGateway.getFirst(navigateActivity.getContentResolver(),
                individualGateway.findByExtIdPrefixDescending(individualExtId));
    }

    private static boolean isIndividualDeceased(Individual selectedIndividual) {

        return selectedIndividual.getEndType().equals(ProjectResources.Individual.END_TYPE_DEATH);

    }

    private static boolean isIndividualFemale(Individual selectedIndividual) {

        return selectedIndividual.getGender().equals(ProjectResources.Individual.GENDER_FEMALE);

    }

    private static boolean isIndividualOutMigrated(Individual selectedIndividual) {

        return selectedIndividual.getEndType().equals(ProjectResources.Individual.RESIDENCY_END_TYPE_OMG);

    }

}
