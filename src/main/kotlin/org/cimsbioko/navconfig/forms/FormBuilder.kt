package org.cimsbioko.navconfig.forms

import org.jdom2.Document

interface FormBuilder {
    fun build(blankDataDoc: Document, ctx: LaunchContext)
}