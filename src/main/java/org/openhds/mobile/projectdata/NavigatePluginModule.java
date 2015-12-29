package org.openhds.mobile.projectdata;

import java.util.List;
import java.util.Map;

import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.model.form.FormBehaviour;
import org.openhds.mobile.projectdata.QueryHelpers.QueryHelper;

public interface NavigatePluginModule {

	public QueryHelper getQueryHelper();

    public ModuleUiHelper getModuleUiHelper();

    public HierarchyInfo getHierarchyInfo();

	public Map<String, List<FormBehaviour>> getFormsForStates();

	public Map<String, DetailFragment> getDetailFragsForStates();
	
}
