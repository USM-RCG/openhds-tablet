package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.projectdata.FormPayloadBuilders.LaunchContext;

/**
 * Determine the visibility of forms in {@link org.openhds.mobile.activity.HierarchyNavigatorActivity}. These are not
 * necessarily 1:1 for forms types.
 */
public interface FormFilter {

    /**
     * Given the specified launch context, returns whether the form should be displayed.
     *
     * @param ctx the launch context to consider
     * @return whether the form, represented by this filter object, is relevant for the context.
     */
    boolean shouldDisplay(LaunchContext ctx);

}
