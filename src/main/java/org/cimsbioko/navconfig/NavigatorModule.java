package org.cimsbioko.navconfig;

import org.cimsbioko.fragment.navigate.detail.DetailFragment;
import org.cimsbioko.navconfig.forms.Binding;
import org.cimsbioko.navconfig.forms.Launcher;

import java.util.List;
import java.util.Map;

public interface NavigatorModule {

    String getName();

    String getActivityTitle();

    String getLaunchLabel();

    String getLaunchDescription();

    Map<String, Binding> getBindings();

    List<Launcher> getLaunchers(String level);

    DetailFragment getDetailFragment(String level);

}
