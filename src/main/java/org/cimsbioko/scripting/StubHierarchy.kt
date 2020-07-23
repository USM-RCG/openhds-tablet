package org.cimsbioko.scripting

import org.cimsbioko.navconfig.Hierarchy

internal class StubHierarchy : Hierarchy {
    override val levelLabels: Map<String, String> = emptyMap()
    override val adminLevels: List<String> = emptyList()
    override val levels: List<String> = emptyList()
    override fun getParentLevel(level: String): String? = null
}