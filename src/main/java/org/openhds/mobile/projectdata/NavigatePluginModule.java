package org.openhds.mobile.projectdata;

import java.util.List;
import java.util.Map;

import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.model.form.FormBehaviour;
import org.openhds.mobile.projectdata.QueryHelpers.QueryHelper;

public interface NavigatePluginModule {

	QueryHelper getQueryHelper();

    ModuleUiHelper getModuleUiHelper();

    HierarchyInfo getHierarchyInfo();

	Map<String, List<FormBehaviour>> getFormsForStates();

	Map<String, DetailFragment> getDetailFragsForStates();
	
}
