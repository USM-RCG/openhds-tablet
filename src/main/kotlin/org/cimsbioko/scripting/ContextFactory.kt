package org.cimsbioko.scripting

import org.mozilla.javascript.Context

/**
 * Custom context factory for consistent creation of contexts when entering js, including through the vm bridge.
 */
class ContextFactory private constructor() : org.mozilla.javascript.ContextFactory() {

    override fun makeContext(): Context = super.makeContext().apply {
        optimizationLevel = -1
        languageVersion = Context.VERSION_ES6
    }

    companion object {
        @JvmStatic
        fun register() {
            if (!hasExplicitGlobal()) {
                initGlobal(ContextFactory())
            }
        }
    }
}