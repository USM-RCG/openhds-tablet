package org.openhds.mobile.navconfig.forms.filters;

import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.navconfig.forms.UsedByJSConfig;

/**
 * A filter implementation that wraps another filter and inverts it's logic.
 */
@UsedByJSConfig
public class InvertedFilter implements FormFilter {

    private FormFilter filterToInvert;

    protected InvertedFilter(FormFilter filter) {
        this.filterToInvert = filter;
    }

    @Override
    public boolean shouldDisplay(LaunchContext ctx) {
        return !filterToInvert.shouldDisplay(ctx);
    }

    public static FormFilter invert(FormFilter filter) {
        return new InvertedFilter(filter);
    }
}
