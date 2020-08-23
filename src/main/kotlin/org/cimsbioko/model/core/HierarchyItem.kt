package org.cimsbioko.model.core

import org.cimsbioko.data.DataWrapper

interface HierarchyItem {
    val level: String
    val hierarchyId: String
    val wrapped: DataWrapper
}