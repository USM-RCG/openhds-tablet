package org.openhds.mobile.projectdata;

import java.util.List;
import java.util.Map;

/**
 *
 * HierarchyInfo is used as a vehicle for hierarchy state information like
 * stateSequence, stateLabels (for UI use), and the name of the particular hierarchy.
 *
 * Multiple NavigatePluginModules can point to the same HierarchyInfo, meaning they depend on the
 * same state information.
 *
 * waffle
 */
public interface HierarchyInfo {

    public String getHierarchyName();

    public Map<String, Integer> getStateLabels();

    public List<String> getStateSequence();

}
