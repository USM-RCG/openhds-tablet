package org.cimsbioko.navconfig.forms.filters;

import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.navconfig.forms.UsedByJSConfig;

import static org.cimsbioko.navconfig.Hierarchy.HOUSEHOLD;
import static org.cimsbioko.repository.GatewayRegistry.getIndividualGateway;

public class CensusFormFilters {

    @UsedByJSConfig
    public static class AddHeadOfHousehold implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return !getIndividualGateway().findByResidency(ctx.getHierarchyPath().get(HOUSEHOLD).getUuid())
                    .exists();
        }
    }
}
