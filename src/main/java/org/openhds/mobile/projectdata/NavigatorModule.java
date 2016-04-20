package org.openhds.mobile.projectdata;

import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.model.form.FormBehavior;

import java.util.List;
import java.util.Map;

public interface NavigatorModule {

    String getLaunchLabel();

    String getLaunchDescription();

    String getActivityTitle();

    HierarchyInfo getHierarchyInfo();

    List<FormBehavior> getFormsForState(String state);

    Map<String, DetailFragment> getDetailFragsForStates();

}
