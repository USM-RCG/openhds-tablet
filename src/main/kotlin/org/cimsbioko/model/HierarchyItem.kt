package org.cimsbioko.model

import org.cimsbioko.data.DataWrapper

interface HierarchyItem {
    val level: String
    val hierarchyId: String
    val wrapped: DataWrapper
}