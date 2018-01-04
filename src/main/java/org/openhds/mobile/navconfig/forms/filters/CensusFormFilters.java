package org.openhds.mobile.navconfig.forms.filters;

import android.content.ContentResolver;

import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.navconfig.forms.UsedByJSConfig;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import static org.openhds.mobile.navconfig.BiokoHierarchy.HOUSEHOLD;

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
