package org.cimsbioko.scripting

import org.cimsbioko.navconfig.Hierarchy

internal class StubHierarchy : Hierarchy {
    override fun getLevelLabels(): Map<String, String> = emptyMap()
    override fun getAdminLevels(): List<String> = emptyList()
    override fun getLevels(): List<String> = emptyList()
    override fun getParentLevel(level: String): String? = null
}