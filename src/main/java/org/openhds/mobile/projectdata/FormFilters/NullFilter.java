package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.projectdata.FormPayloadBuilders.LaunchContext;

/**
 * A default filter implementation to handle case where there should be no filtering.
 */
public class NullFilter implements FormFilter {

    public static final NullFilter INSTANCE = new NullFilter();

    @Override
    public boolean shouldDisplay(LaunchContext ctx) {
        return true;
    }
}
