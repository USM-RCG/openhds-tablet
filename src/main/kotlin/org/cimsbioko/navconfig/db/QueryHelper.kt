package org.cimsbioko.navconfig.db

import org.cimsbioko.data.DataWrapper
import org.cimsbioko.data.Gateway
import org.cimsbioko.data.GatewayRegistry.individualGateway
import org.cimsbioko.data.GatewayRegistry.locationGateway
import org.cimsbioko.data.GatewayRegistry.locationHierarchyGateway
import org.cimsbioko.data.LocationHierarchyGateway
import org.cimsbioko.data.Query
import org.cimsbioko.model.core.HierarchyItem
import org.cimsbioko.navconfig.Hierarchy
import org.cimsbioko.navconfig.NavigatorConfig

interface QueryHelper {
    fun getAll(level: String): Query<out HierarchyItem>?
    fun getChildren(parent: DataWrapper, childLevel: String): Query<out HierarchyItem>?
    operator fun get(level: String, uuid: String): Query<out HierarchyItem>?
    fun getParent(level: String, uuid: String): Query<out HierarchyItem>?
    fun getByHierarchyId(id: String): Query<out HierarchyItem>?
}

object DefaultQueryHelper : QueryHelper {

    private fun isAdminLevel(level: String): Boolean = NavigatorConfig.instance.adminLevels.contains(level)

    private fun isTopLevel(level: String): Boolean = NavigatorConfig.instance.topLevel == level

    private fun isLastAdminLevel(level: String): Boolean =
            NavigatorConfig.instance.adminLevels.let { it.isNotEmpty() && it[it.lastIndex] == level }

    private fun getLevelGateway(level: String): Gateway<out HierarchyItem>? = when {
        Hierarchy.HOUSEHOLD == level -> locationGateway
        Hierarchy.INDIVIDUAL == level -> individualGateway
        isAdminLevel(level) -> locationHierarchyGateway
        else -> null
    }

    override fun getAll(level: String): Query<out HierarchyItem>? = when (val gateway = getLevelGateway(level)) {
        is LocationHierarchyGateway -> gateway.findByLevel(level)
        else -> gateway?.findAll()
    }

    override fun getChildren(parent: DataWrapper, childLevel: String): Query<out HierarchyItem>? = parent.category.let { lvl ->
        when {
            isLastAdminLevel(lvl) -> locationGateway.findByHierarchy(parent.uuid)
            isAdminLevel(lvl) -> locationHierarchyGateway.findByParent(parent.uuid)
            Hierarchy.HOUSEHOLD == lvl -> individualGateway.findByResidency(parent.uuid)
            else -> null
        }
    }

    override fun get(level: String, uuid: String): Query<out HierarchyItem>? {
        return getLevelGateway(level)?.findById(uuid)
    }

    override fun getParent(level: String, uuid: String): Query<out HierarchyItem>? =
            NavigatorConfig.instance.getParentLevel(level)?.let { pl ->
                when {
                    level.let { isAdminLevel(it) && !isTopLevel(it) } -> locationHierarchyGateway.findById(uuid).first?.parentUuid
                    level == Hierarchy.HOUSEHOLD -> locationGateway.findById(uuid).first?.hierarchyUuid
                    level == Hierarchy.INDIVIDUAL -> individualGateway.findById(uuid).first?.currentResidenceUuid
                    else -> null
                }?.let { get(pl, it) }
            }

    override fun getByHierarchyId(id: String): Query<out HierarchyItem>? = id.split(":".toRegex()).let { parts ->
        if (parts.size == 2) parts.let { (level, uuid) -> get(level, uuid) } else null
    }
}
