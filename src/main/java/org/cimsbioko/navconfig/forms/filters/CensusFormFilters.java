package org.cimsbioko.navconfig.forms.filters;

import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.navconfig.forms.UsedByJSConfig;
import org.cimsbioko.repository.gateway.IndividualGateway;

import static org.cimsbioko.navconfig.Hierarchy.HOUSEHOLD;

public class CensusFormFilters {

    @UsedByJSConfig
    public static class AddHeadOfHousehold implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            IndividualGateway individualGateway = new IndividualGateway();
            String locationUuid = ctx.getHierarchyPath().get(HOUSEHOLD).getUuid();
            return individualGateway.getFirst(individualGateway.findByResidency(locationUuid)) == null;
        }
    }
}
