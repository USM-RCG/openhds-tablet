package org.cimsbioko.scripting;

import org.cimsbioko.navconfig.Hierarchy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class StubHierarchy implements Hierarchy {

    @Override
    public Map<String, String> getLevelLabels() {
        return Collections.emptyMap();
    }

    @Override
    public List<String> getAdminLevels() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getLevels() {
        return Collections.emptyList();
    }

    @Override
    public String getParentLevel(String level) {
        return null;
    }
}
