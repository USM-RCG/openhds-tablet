package org.openhds.mobile.navconfig;

import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.navconfig.forms.Binding;
import org.openhds.mobile.navconfig.forms.Launcher;

import java.util.List;
import java.util.Map;

public interface NavigatorModule {

    String getActivityTitle();

    String getLaunchLabel();

    String getLaunchDescription();

    Map<String, Binding> getBindings();

    List<Launcher> getLaunchers(String level);

    DetailFragment getDetailFragment(String level);

}
