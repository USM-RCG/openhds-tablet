package org.cimsbioko.navconfig

import org.cimsbioko.model.HierarchyItem

interface HierFormatter {
    fun formatItem(item: HierarchyItem): HierItemDisplay
}

interface HierItemDisplay {
    val heading: String
    val subheading: String
    val details: Map<String, String>?
}