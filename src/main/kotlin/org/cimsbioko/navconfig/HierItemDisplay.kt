package org.cimsbioko.navconfig

import org.cimsbioko.model.core.HierarchyItem

interface HierFormatter {
    fun formatItem(item: HierarchyItem): HierItemDisplay
}

interface HierItemDisplay {
    val heading: String
    val subheading: String
    val details: Map<String, String>?
}