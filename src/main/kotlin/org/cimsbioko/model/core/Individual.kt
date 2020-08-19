package org.cimsbioko.model.core

import org.cimsbioko.navconfig.UsedByJSConfig
import java.io.Serializable

data class Individual(
        var uuid: String? = null,
        var extId: String? = null,
        var firstName: String? = null,
        var lastName: String? = null,
        var gender: String? = null,
        var dob: String? = null,
        var currentResidenceUuid: String? = null,
        var relationshipToHead: String? = null,
        var otherId: String? = null,
        var otherNames: String? = null,
        var phoneNumber: String? = null,
        var otherPhoneNumber: String? = null,
        var pointOfContactName: String? = null,
        var pointOfContactPhoneNumber: String? = null,
        var languagePreference: String? = null,
        var status: String? = null,
        var nationality: String? = null,
        var attrs: String? = null
) : HierarchyItem, Serializable {
    companion object {
        @UsedByJSConfig
        @JvmStatic
        fun Individual.getFullName(): String = "$firstName $lastName"
    }
}