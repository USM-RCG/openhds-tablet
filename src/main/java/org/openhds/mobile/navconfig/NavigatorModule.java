package org.openhds.mobile.navconfig;

import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.model.form.FormBehavior;

import java.util.List;
import java.util.Map;

public interface NavigatorModule {

    String getLaunchLabel();

    String getLaunchDescription();

    String getActivityTitle();

    List<FormBehavior> getForms(String level);

    Map<String, DetailFragment> getDetailFragsForStates();

    Map<String, String> getFormLabels();

}
