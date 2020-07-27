package org.cimsbioko.scripting

import org.mozilla.javascript.Scriptable

class MessagesScriptable internal constructor(private val config: JsConfig) : Scriptable {
    override fun getClassName() = "MessagesScriptable"
    override fun get(name: String, start: Scriptable): String = config.getString(name)
    override fun get(index: Int, start: Scriptable): Any? = null
    override fun has(name: String, start: Scriptable) = true
    override fun has(index: Int, start: Scriptable): Boolean = false
    override fun put(name: String, start: Scriptable, value: Any) {}
    override fun put(index: Int, start: Scriptable, value: Any) {}
    override fun delete(name: String) {}
    override fun delete(index: Int) {}
    override fun getPrototype(): Scriptable? = null
    override fun setPrototype(prototype: Scriptable) {}
    override fun getParentScope(): Scriptable? = null
    override fun setParentScope(parent: Scriptable) {}
    override fun getIds(): Array<Any>? = null
    override fun getDefaultValue(hint: Class<*>?): Any? = null
    override fun hasInstance(instance: Scriptable): Boolean = false
}