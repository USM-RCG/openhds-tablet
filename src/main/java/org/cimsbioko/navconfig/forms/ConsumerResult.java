package org.cimsbioko.navconfig.forms;

import org.cimsbioko.navconfig.UsedByJSConfig;

/**
 * Represents the result of consuming a form.
 */
public class ConsumerResult {

    private final boolean augmentInstance;

    @UsedByJSConfig
    public ConsumerResult(boolean augmentInstance) {
        this.augmentInstance = augmentInstance;
    }

    public boolean hasInstanceUpdates() {
        return augmentInstance;
    }

}
