package org.cimsbioko.navconfig

import org.cimsbioko.fragment.navigate.detail.DetailFragment
import org.cimsbioko.navconfig.forms.Binding
import org.cimsbioko.navconfig.forms.Launcher

interface NavigatorModule {
    val name: String
    val activityTitle: String
    val launchLabel: String
    val launchDescription: String
    val bindings: Map<String, Binding>
    fun getLaunchers(level: String): List<Launcher>
    fun getDetailFragment(level: String): DetailFragment
}