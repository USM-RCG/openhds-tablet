package org.cimsbioko.model.core

import java.io.Serializable

data class FieldWorker(
        var uuid: String? = null,
        var extId: String? = null,
        var firstName: String? = null,
        var lastName: String? = null,
        var passwordHash: String? = null,
        var idPrefix: String? = null
) : Serializable