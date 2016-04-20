package org.openhds.mobile.projectdata;

import java.util.List;
import java.util.Map;

import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.model.form.FormBehavior;

public interface NavigatePluginModule {

    ModuleUiHelper getModuleUiHelper();

    HierarchyInfo getHierarchyInfo();

	List<FormBehavior> getFormsForState(String state);

	Map<String, DetailFragment> getDetailFragsForStates();
	
}
