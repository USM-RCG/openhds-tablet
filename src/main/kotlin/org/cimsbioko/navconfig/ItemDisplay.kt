package org.cimsbioko.navconfig

import org.cimsbioko.model.HierarchyItem

interface ItemFormatter {
    fun format(item: HierarchyItem): ItemDetails
}

interface ItemDetails {
    val banner: String?
    val sections: List<DetailsSection>?
}

interface DetailsSection {
    val banner: String?
    val details: Map<String, String?>?
}