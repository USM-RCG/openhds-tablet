package org.cimsbioko.navconfig

interface NavigatorModule {
    val name: String
    val activityTitle: String
    val launchLabel: String
    val launchDescription: String
    val bindings: Map<String, Binding>
    fun getLaunchers(level: String): List<Launcher>
    fun getItemFormatter(level: String): ItemFormatter?
    fun getHierFormatter(level: String): HierFormatter?
}