package org.cimsbioko.navconfig.forms

import org.cimsbioko.data.DataWrapper
import org.cimsbioko.model.core.FieldWorker
import org.cimsbioko.navconfig.HierarchyPath
import org.cimsbioko.navconfig.UsedByJSConfig

/**
 * This is the formal contract currently required to build a payload and launch a form. Currently,
 * [HierarchyNavigatorActivity] implements this contract directly. However, this ensures that
 * that dependency can be easily identified and, if necessary, decoupled.
 */
interface LaunchContext {
    @get:UsedByJSConfig
    val currentFieldWorker: FieldWorker?
    val currentSelection: DataWrapper?

    @get:UsedByJSConfig
    val hierarchyPath: HierarchyPath?
}