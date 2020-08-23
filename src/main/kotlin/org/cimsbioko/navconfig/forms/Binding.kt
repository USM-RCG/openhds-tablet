package org.cimsbioko.navconfig.forms

import org.cimsbioko.data.DataWrapper
import org.cimsbioko.model.core.FieldWorker
import org.cimsbioko.navconfig.HierarchyPath
import org.cimsbioko.navconfig.UsedByJSConfig
import org.jdom2.Document

interface Binding {
    val name: String
    val form: String
    val label: String
    val builder: FormBuilder
    val consumer: FormConsumer
    val formatter: FormFormatter
}

/**
 * This is the formal contract currently required to build and launch a form. Currently,
 * [org.cimsbioko.activity.HierarchyNavigatorActivity] implements this contract directly. However, this ensures that
 * that dependency can be easily identified and, if necessary, decoupled.
 */
interface LaunchContext {
    @get:UsedByJSConfig
    val currentFieldWorker: FieldWorker?
    val currentSelection: DataWrapper?

    @get:UsedByJSConfig
    val hierarchyPath: HierarchyPath?
}

interface FormBuilder {
    fun build(blankDataDoc: Document, ctx: LaunchContext)
}

interface FormConsumer {
    fun consume(dataDoc: Document, ctx: LaunchContext): Boolean
}

interface FormFormatter {
    fun format(dataDoc: Document): FormDisplay
}

interface FormDisplay {
    val fieldworker: String?
    val entity: String?
    val dateTimeCollected: String?
    val extra1: String?
    val extra2: String?
}

interface Launcher {
    val label: String
    fun relevantFor(ctx: LaunchContext): Boolean
    val binding: Binding
}