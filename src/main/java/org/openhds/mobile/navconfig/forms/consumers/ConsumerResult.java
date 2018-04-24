package org.openhds.mobile.navconfig.forms.consumers;

/**
 * Represents the result of consuming a form.
 */
public class ConsumerResult {

    private final boolean augmentInstance;

    public ConsumerResult(boolean augmentInstance) {
        this.augmentInstance = augmentInstance;
    }

    public boolean hasInstanceUpdates() {
        return augmentInstance;
    }

}
