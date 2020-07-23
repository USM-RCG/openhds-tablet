package org.cimsbioko.navconfig.db

import org.cimsbioko.data.DataWrapper
import org.cimsbioko.data.Gateway
import org.cimsbioko.data.GatewayRegistry.*
import org.cimsbioko.data.LocationHierarchyGateway
import org.cimsbioko.navconfig.Hierarchy
import org.cimsbioko.navconfig.NavigatorConfig

interface QueryHelper {
    fun getAll(level: String): List<DataWrapper>
    fun getChildren(parent: DataWrapper, childLevel: String): List<DataWrapper>
    operator fun get(level: String, uuid: String): DataWrapper?
    fun getParent(level: String, uuid: String): DataWrapper?
}

object DefaultQueryHelper : QueryHelper {

    private fun isAdminLevel(level: String): Boolean = NavigatorConfig.instance.adminLevels.contains(level)

    private fun isTopLevel(level: String): Boolean = NavigatorConfig.instance.topLevel == level

    private fun isLastAdminLevel(level: String): Boolean =
            NavigatorConfig.instance.adminLevels.let { it.isNotEmpty() && it[it.lastIndex] == level }

    private fun getLevelGateway(level: String): Gateway<*>? = when {
        Hierarchy.HOUSEHOLD == level -> getLocationGateway()
        Hierarchy.INDIVIDUAL == level -> getIndividualGateway()
        isAdminLevel(level) -> getLocationHierarchyGateway()
        else -> null
    }

    override fun getAll(level: String): List<DataWrapper> = when (val gateway = getLevelGateway(level)) {
        is LocationHierarchyGateway -> gateway.findByLevel(level).wrapperList
        else -> gateway?.findAll()?.wrapperList ?: emptyList()
    }

    override fun getChildren(parent: DataWrapper, childLevel: String): List<DataWrapper> = parent.category?.let { lvl ->
        when {
            isLastAdminLevel(lvl) -> getLocationGateway().findByHierarchy(parent.uuid).wrapperList
            isAdminLevel(lvl) -> getLocationHierarchyGateway().findByParent(parent.uuid).wrapperList
            Hierarchy.HOUSEHOLD == lvl -> getIndividualGateway().findByResidency(parent.uuid).wrapperList
            else -> null
        }
    } ?: emptyList()

    override fun get(level: String, uuid: String): DataWrapper? = getLevelGateway(level)?.findById(uuid)?.firstWrapper

    override fun getParent(level: String, uuid: String): DataWrapper? =
            NavigatorConfig.instance.getParentLevel(level)?.let { pl ->
                when {
                    level.let { isAdminLevel(it) && !isTopLevel(it) } -> getLocationHierarchyGateway().findById(uuid).first?.parentUuid
                    level == Hierarchy.HOUSEHOLD -> getLocationGateway().findById(uuid).first?.hierarchyUuid
                    level == Hierarchy.INDIVIDUAL -> getIndividualGateway().findById(uuid).first?.currentResidenceUuid
                    else -> null
                }?.let { get(pl, it) }
            }
}
