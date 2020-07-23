package org.cimsbioko.navconfig.db

import org.cimsbioko.data.DataWrapper
import org.cimsbioko.data.Gateway
import org.cimsbioko.data.GatewayRegistry
import org.cimsbioko.navconfig.Hierarchy
import org.cimsbioko.navconfig.NavigatorConfig
import java.util.ArrayList

interface QueryHelper {
    fun getAll(level: String): List<DataWrapper>
    fun getChildren(parent: DataWrapper, childLevel: String): List<DataWrapper>
    operator fun get(level: String, uuid: String): DataWrapper?
    fun getParent(level: String, uuid: String): DataWrapper?
}

object DefaultQueryHelper : QueryHelper {

    private fun isAdminLevel(level: String): Boolean = NavigatorConfig.getInstance().adminLevels.contains(level)

    private fun isTopLevel(level: String): Boolean = NavigatorConfig.getInstance().topLevel == level

    private fun isLastAdminLevel(level: String): Boolean {
        return NavigatorConfig.getInstance().adminLevels.let { it.isNotEmpty() && it[it.lastIndex] == level }
    }

    private fun getLevelGateway(level: String): Gateway<*>? = if (isAdminLevel(level)) {
        GatewayRegistry.getLocationHierarchyGateway()
    } else {
        when (level) {
            Hierarchy.HOUSEHOLD -> GatewayRegistry.getLocationGateway()
            Hierarchy.INDIVIDUAL -> GatewayRegistry.getIndividualGateway()
            else -> null
        }
    }

    override fun getAll(level: String): List<DataWrapper> {
        if (isAdminLevel(level)) {
            return GatewayRegistry.getLocationHierarchyGateway().findByLevel(level).wrapperList
        }
        when (level) {
            Hierarchy.HOUSEHOLD, Hierarchy.INDIVIDUAL -> {
                val gateway = getLevelGateway(level)
                if (gateway != null) {
                    return gateway.findAll().wrapperList
                }
            }
        }
        return ArrayList()
    }

    override fun getChildren(parent: DataWrapper, childLevel: String): List<DataWrapper> {
        val level = parent.category
        return if (isAdminLevel(level)) {
            if (isLastAdminLevel(level)) {
                GatewayRegistry.getLocationGateway().findByHierarchy(parent.uuid).wrapperList
            } else {
                GatewayRegistry.getLocationHierarchyGateway().findByParent(parent.uuid).wrapperList
            }
        } else if (Hierarchy.HOUSEHOLD == level) {
            GatewayRegistry.getIndividualGateway().findByResidency(parent.uuid).wrapperList
        } else ArrayList()
    }

    override fun get(level: String, uuid: String): DataWrapper? {
        return getLevelGateway(level)?.findById(uuid)?.firstWrapper
    }

    override fun getParent(level: String, uuid: String): DataWrapper? {
        val parentLevel = NavigatorConfig.getInstance().getParentLevel(level)
        return if (isAdminLevel(level) && !isTopLevel(level)) {
            get(parentLevel, GatewayRegistry.getLocationHierarchyGateway().findById(uuid).first.parentUuid)
        } else {
            when (level) {
                Hierarchy.HOUSEHOLD -> {
                    get(parentLevel, GatewayRegistry.getLocationGateway().findById(uuid).first.hierarchyUuid)
                }
                Hierarchy.INDIVIDUAL -> {
                    get(parentLevel, GatewayRegistry.getIndividualGateway().findById(uuid).first.currentResidenceUuid)
                }
                else -> null
            }
        }
    }
}