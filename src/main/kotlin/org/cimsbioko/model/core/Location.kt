package org.cimsbioko.model.core

import java.io.Serializable

data class Location(
        var uuid: String? = null,
        var extId: String? = null,
        var name: String? = null,
        var latitude: String? = null,
        var longitude: String? = null,
        var hierarchyUuid: String? = null,
        var description: String? = null,
        var attrs: String? = null
) : HierarchyItem, Serializable