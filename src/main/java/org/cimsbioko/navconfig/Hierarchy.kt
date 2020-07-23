package org.cimsbioko.navconfig

interface Hierarchy {

    val levelLabels: Map<String, String>
    val adminLevels: List<String>
    val levels: List<String>
    fun getParentLevel(level: String): String?

    companion object {
        const val HOUSEHOLD = "household"
        const val INDIVIDUAL = "individual"
    }
}