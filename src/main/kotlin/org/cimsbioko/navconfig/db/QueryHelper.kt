package org.cimsbioko.navconfig.db

import org.cimsbioko.data.DataWrapper
import org.cimsbioko.data.Gateway
import org.cimsbioko.data.GatewayRegistry.individualGateway
import org.cimsbioko.data.GatewayRegistry.locationGateway
import org.cimsbioko.data.GatewayRegistry.locationHierarchyGateway
import org.cimsbioko.data.LocationHierarchyGateway
import org.cimsbioko.model.core.HierarchyItem
import org.cimsbioko.navconfig.Hierarchy
import org.cimsbioko.navconfig.NavigatorConfig

interface QueryHelper {
    fun getAll(level: String): List<DataWrapper>
    fun getChildren(parent: DataWrapper, childLevel: String): List<DataWrapper>
    operator fun get(level: String, uuid: String): DataWrapper?
    fun getUnwrapped(level: String, uuid: String): HierarchyItem?
    fun getParent(level: String, uuid: String): DataWrapper?
}

object DefaultQueryHelper : QueryHelper {

    private fun isAdminLevel(level: String): Boolean = NavigatorConfig.instance.adminLevels.contains(level)

    private fun isTopLevel(level: String): Boolean = NavigatorConfig.instance.topLevel == level

    private fun isLastAdminLevel(level: String): Boolean =
            NavigatorConfig.instance.adminLevels.let { it.isNotEmpty() && it[it.lastIndex] == level }

    private fun getLevelGateway(level: String): Gateway<*>? = when {
        Hierarchy.HOUSEHOLD == level -> locationGateway
        Hierarchy.INDIVIDUAL == level -> individualGateway
        isAdminLevel(level) -> locationHierarchyGateway
        else -> null
    }

    override fun getAll(level: String): List<DataWrapper> = when (val gateway = getLevelGateway(level)) {
        is LocationHierarchyGateway -> gateway.findByLevel(level).wrapperList
        else -> gateway?.findAll()?.wrapperList ?: emptyList()
    }

    override fun getChildren(parent: DataWrapper, childLevel: String): List<DataWrapper> = parent.category.let { lvl ->
        when {
            isLastAdminLevel(lvl) -> locationGateway.findByHierarchy(parent.uuid).wrapperList
            isAdminLevel(lvl) -> locationHierarchyGateway.findByParent(parent.uuid).wrapperList
            Hierarchy.HOUSEHOLD == lvl -> individualGateway.findByResidency(parent.uuid).wrapperList
            else -> null
        }
    } ?: emptyList()

    override fun get(level: String, uuid: String): DataWrapper? = getLevelGateway(level)?.findById(uuid)?.firstWrapper

    override fun getUnwrapped(level: String, uuid: String) =
            getLevelGateway(level)?.findById(uuid)?.first as? HierarchyItem


    override fun getParent(level: String, uuid: String): DataWrapper? =
            NavigatorConfig.instance.getParentLevel(level)?.let { pl ->
                when {
                    level.let { isAdminLevel(it) && !isTopLevel(it) } -> locationHierarchyGateway.findById(uuid).first?.parentUuid
                    level == Hierarchy.HOUSEHOLD -> locationGateway.findById(uuid).first?.hierarchyUuid
                    level == Hierarchy.INDIVIDUAL -> individualGateway.findById(uuid).first?.currentResidenceUuid
                    else -> null
                }?.let { get(pl, it) }
            }
}
