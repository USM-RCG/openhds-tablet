package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.projectdata.FormPayloadBuilders.LaunchContext;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import static org.openhds.mobile.projectdata.BiokoHierarchy.HOUSEHOLD_STATE;
import static org.openhds.mobile.projectdata.BiokoHierarchy.INDIVIDUAL_STATE;


public class UpdateFormFilters {

    public static class StartAVisit implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return ctx.getCurrentVisit() == null;
        }
    }

    public static class RegisterInMigration implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return ctx.getCurrentVisit() != null && ctx.getHierarchyPath().get(HOUSEHOLD_STATE) != null;
        }
    }

    public static class DeathOrOutMigrationFilter implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return couldObserveDeathOrOutMigration(ctx);
        }
    }

    private static boolean couldObserveDeathOrOutMigration(LaunchContext ctx) {
        Individual indiv = getIndividual(ctx);
        return ctx.getCurrentVisit() != null && !isDeceased(indiv) && !isOutMigrated(indiv);
    }

    public static class PregnancyFilter implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return couldObservePregnancy(ctx);
        }
    }

    private static Individual getIndividual(LaunchContext ctx) {
        String uuid = ctx.getHierarchyPath().get(INDIVIDUAL_STATE).getUuid();
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        return individualGateway.getFirst(ctx.getContentResolver(), individualGateway.findById(uuid));
    }

    private static boolean couldObservePregnancy(LaunchContext ctx) {
        Individual indiv = getIndividual(ctx);
        return ctx.getCurrentVisit() != null && isFemale(indiv) && !isDeceased(indiv) && !isOutMigrated(indiv);
    }

    private static boolean isDeceased(Individual individual) {
        return individual.getEndType().equals(ProjectResources.Individual.END_TYPE_DEATH);
    }

    private static boolean isFemale(Individual individual) {
        return individual.getGender().equals(ProjectResources.Individual.GENDER_FEMALE);
    }

    private static boolean isOutMigrated(Individual individual) {
        return individual.getEndType().equals(ProjectResources.Individual.RESIDENCY_END_TYPE_OMG);
    }

}
