package org.openhds.mobile.navconfig.forms.consumers;

import org.openhds.mobile.model.form.FormBehavior;

import java.util.Map;

/**
 * Simple little class that allows the ability to string multiple forms together in a sequence
 * <p/>
 * -waffle
 */
public class ConsumerResult {

    private final boolean augmentInstance;
    private final FormBehavior followUp;
    private final Map<String, String> followUpHints;

    public ConsumerResult(boolean augmentInstance, FormBehavior followUp, Map<String, String> followUpHints) {
        this.augmentInstance = augmentInstance;
        this.followUp = followUp;
        this.followUpHints = followUpHints;
    }

    public boolean hasInstanceUpdates() {
        return augmentInstance;
    }

    public FormBehavior getFollowUp() {
        return followUp;
    }

    public boolean hasFollowUp() {
        return followUp != null;
    }

    public Map<String, String> getFollowUpHints() {
        return followUpHints;
    }
}
