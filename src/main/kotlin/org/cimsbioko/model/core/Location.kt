package org.cimsbioko.model.core

import org.cimsbioko.data.DataWrapper
import org.cimsbioko.navconfig.Hierarchy
import java.io.Serializable

data class Location(
        var uuid: String,
        var extId: String,
        var name: String,
        var latitude: String? = null,
        var longitude: String? = null,
        var hierarchyUuid: String? = null,
        var description: String? = null,
        var attrs: String? = null
) : HierarchyItem, Serializable {
    override val wrapped: DataWrapper
        get() = DataWrapper(
                uuid = uuid,
                category = level,
                extId = extId,
                name = name
        )
    override val level: String = Hierarchy.HOUSEHOLD
    override val hierarchyId: String
        get() = "$level:$uuid"
}