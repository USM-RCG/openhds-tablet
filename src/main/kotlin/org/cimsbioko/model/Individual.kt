package org.cimsbioko.model

import org.cimsbioko.data.DataWrapper
import org.cimsbioko.navconfig.Hierarchy
import org.cimsbioko.navconfig.UsedByJSConfig
import java.io.Serializable

data class Individual(
        var uuid: String,
        var extId: String,
        var firstName: String? = null,
        var lastName: String? = null,
        var gender: String? = null,
        var dob: String? = null,
        var currentResidenceUuid: String? = null,
        var otherNames: String? = null,
        var attrs: String? = null
) : HierarchyItem, Serializable {
    override val wrapped: DataWrapper
        get() = DataWrapper(
                uuid = uuid,
                category = level,
                extId = extId,
                name = getFullName()
        )
    override val level: String = Hierarchy.INDIVIDUAL
    override val hierarchyId: String
        get() = "$level:$uuid"

    companion object {
        @UsedByJSConfig
        @JvmStatic
        fun Individual.getFullName(): String = "$firstName $lastName"
    }
}