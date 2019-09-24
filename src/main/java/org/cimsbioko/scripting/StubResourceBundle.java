package org.cimsbioko.scripting;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * A {@link ResourceBundle} implementation used to provide default behavior when a resource bundle is not present.
 */
class StubResourceBundle extends ResourceBundle {

    @Override
    protected Object handleGetObject(String key) {
        return String.format("{%s}", key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(Collections.emptyList());
    }
}
