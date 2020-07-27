package org.cimsbioko.navconfig.forms

interface Launcher {
    val label: String
    fun relevantFor(ctx: LaunchContext): Boolean
    val binding: Binding
}