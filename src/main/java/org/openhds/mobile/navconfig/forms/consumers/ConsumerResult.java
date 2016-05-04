package org.openhds.mobile.navconfig.forms.consumers;

import org.openhds.mobile.navconfig.forms.Binding;

import java.util.Map;

/**
 * Simple little class that allows the ability to string multiple forms together in a sequence
 * <p/>
 * -waffle
 */
public class ConsumerResult {

    private final boolean augmentInstance;
    private final Binding followUp;
    private final Map<String, String> followUpHints;

    public ConsumerResult(boolean augmentInstance, Binding followUp, Map<String, String> followUpHints) {
        this.augmentInstance = augmentInstance;
        this.followUp = followUp;
        this.followUpHints = followUpHints;
    }

    public boolean hasInstanceUpdates() {
        return augmentInstance;
    }

    public Binding getFollowUp() {
        return followUp;
    }

    public boolean hasFollowUp() {
        return followUp != null;
    }

    public Map<String, String> getFollowUpHints() {
        return followUpHints;
    }
}
