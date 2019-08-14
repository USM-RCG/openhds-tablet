package org.cimsbioko.navconfig.forms.filters;

import android.content.ContentResolver;

import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.navconfig.forms.UsedByJSConfig;
import org.cimsbioko.repository.gateway.IndividualGateway;

import static org.cimsbioko.navconfig.BiokoHierarchy.HOUSEHOLD;

public class CensusFormFilters {

    @UsedByJSConfig
    public static class AddLocation implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            return true;
        }
    }

    @UsedByJSConfig
    public static class AddHeadOfHousehold implements FormFilter {
        @Override
        public boolean shouldDisplay(LaunchContext ctx) {
            ContentResolver resolver = ctx.getContentResolver();
            IndividualGateway individualGateway = new IndividualGateway();
            String locationUuid = ctx.getHierarchyPath().get(HOUSEHOLD).getUuid();
            return individualGateway.getFirst(resolver, individualGateway.findByResidency(locationUuid)) == null;
        }
    }
}
