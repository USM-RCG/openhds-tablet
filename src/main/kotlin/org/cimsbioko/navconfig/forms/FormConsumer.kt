package org.cimsbioko.navconfig.forms

import org.jdom2.Document

interface FormConsumer {
    fun consume(dataDoc: Document, ctx: LaunchContext): Boolean
}