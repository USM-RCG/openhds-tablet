package org.openhds.mobile.navconfig.forms.filters;

import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.navconfig.ProjectResources;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import static org.openhds.mobile.navconfig.BiokoHierarchy.HOUSEHOLD;
import static org.openhds.mobile.navconfig.BiokoHierarchy.INDIVIDUAL;


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
            return ctx.getCurrentVisit() != null && ctx.getHierarchyPath().get(HOUSEHOLD) != null;
        }
    }

    public static class DeathOrOutMigrationFilter implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            Individual indiv = getIndividual(ctx);
            return ctx.getCurrentVisit() != null && !isDeceased(indiv) && !isOutMigrated(indiv);
        }
    }

    public static class PregnancyFilter implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            Individual indiv = getIndividual(ctx);
            return ctx.getCurrentVisit() != null && isFemale(indiv) && !isDeceased(indiv) && !isOutMigrated(indiv);
        }
    }

    private static Individual getIndividual(LaunchContext ctx) {
        String uuid = ctx.getHierarchyPath().get(INDIVIDUAL).getUuid();
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        return individualGateway.getFirst(ctx.getContentResolver(), individualGateway.findById(uuid));
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
