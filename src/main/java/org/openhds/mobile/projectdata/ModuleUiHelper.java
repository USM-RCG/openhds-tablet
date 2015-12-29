package org.openhds.mobile.projectdata;

/**
 * ModuleUiHelper is a way of transporting the "look" of a particular NavigatePluginModule.
 *
 * It should contain the data necessary (mainly drawable ids) to send to DataSelectionFragment, FormSelectionFragment,
 * HierarchySelectionFragment, and anything (UI) else that changes depending on the NavigatePluginModule.
 *
 * waffle
 */
public interface ModuleUiHelper {

    public int getModuleLabelStringId();
    public int getModuleDescriptionStringId();
    public int getModulePortalDrawableId();
    public int getModuleTitleStringId();

    public int getDataSelectionDrawableId();
    public int getFormSelectionDrawableId();
    public int getHierarchySelectionDrawableId();

    public int getMiddleColumnDrawableId();

}
