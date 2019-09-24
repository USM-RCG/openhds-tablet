package org.cimsbioko.scripting;

import org.mozilla.javascript.Scriptable;

public class MessagesScriptable implements Scriptable {

    private final JsConfig config;

    MessagesScriptable(JsConfig config) {
        this.config = config;
    }

    @Override
    public String getClassName() {
        return "MessagesScriptable";
    }

    @Override
    public Object get(String name, Scriptable start) {
        return config.getString(name);
    }

    @Override
    public Object get(int index, Scriptable start) {
        return null;
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return true;
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return false;
    }

    @Override
    public void put(String name, Scriptable start, Object value) {

    }

    @Override
    public void put(int index, Scriptable start, Object value) {

    }

    @Override
    public void delete(String name) {

    }

    @Override
    public void delete(int index) {

    }

    @Override
    public Scriptable getPrototype() {
        return null;
    }

    @Override
    public void setPrototype(Scriptable prototype) {

    }

    @Override
    public Scriptable getParentScope() {
        return null;
    }

    @Override
    public void setParentScope(Scriptable parent) {
    }

    @Override
    public Object[] getIds() {
        return null;
    }

    @Override
    public Object getDefaultValue(Class<?> hint) {
        return null;
    }

    @Override
    public boolean hasInstance(Scriptable instance) {
        return false;
    }


}
