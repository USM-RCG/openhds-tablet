package org.cimsbioko.navconfig;

import java.util.List;
import java.util.Map;

public interface Hierarchy {

    String HOUSEHOLD = "household", INDIVIDUAL = "individual";

    Map<String, String> getLevelLabels();

    List<String> getAdminLevels();

    List<String> getLevels();

    String getParentLevel(String level);
}
