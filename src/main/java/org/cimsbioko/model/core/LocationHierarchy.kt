package org.cimsbioko.model.core

import java.io.Serializable

data class LocationHierarchy(
        var extId: String? = null,
        var name: String? = null,
        var parentUuid: String? = null,
        var level: String? = null,
        var uuid: String? = null,
        var attrs: String? = null
) : Serializable