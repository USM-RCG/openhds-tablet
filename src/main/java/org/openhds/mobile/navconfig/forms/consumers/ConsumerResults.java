package org.openhds.mobile.navconfig.forms.consumers;

import org.openhds.mobile.model.form.FormBehavior;

import java.util.Map;

/**
 *
 *  Simple little class that allows the ability to string multiple forms together in a sequence
 *
 *  -waffle
 */
public class ConsumerResults {

    private final boolean needsPostfill;
    private final FormBehavior followUpFormBehavior;
    private final Map<String, String> followUpFormHints;

    public ConsumerResults(boolean needsPostfill, FormBehavior followUpFormBehavior, Map<String,String> followUpFormHints){
        this.needsPostfill = needsPostfill;
        this.followUpFormBehavior = followUpFormBehavior;
        this.followUpFormHints = followUpFormHints;
    }

    public boolean needsPostfill() {
        return needsPostfill;
    }

    public FormBehavior getFollowUpFormBehavior() {
        return followUpFormBehavior;
    }

    public Map<String, String> getFollowUpFormHints() {
        return followUpFormHints;
    }


}
