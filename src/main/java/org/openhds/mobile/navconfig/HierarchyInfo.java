package org.openhds.mobile.navconfig;

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

    Map<String, Integer> getLevelLabels();

    List<String> getLevels();

}
