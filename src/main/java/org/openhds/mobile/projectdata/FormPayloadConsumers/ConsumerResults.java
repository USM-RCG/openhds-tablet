package org.openhds.mobile.projectdata.FormPayloadConsumers;

import org.openhds.mobile.model.form.FormBehaviour;

import java.util.Map;

/**
 *
 *  Simple little class that allows the ability to string multiple forms together in a sequence
 *
 *  -waffle
 */
public class ConsumerResults {

    private final boolean needsPostfill;
    private final FormBehaviour followUpFormBehaviour;
    private final Map<String, String> followUpFormHints;

    public ConsumerResults(boolean needsPostfill, FormBehaviour followUpFormBehaviour, Map<String,String> followUpFormHints){
        this.needsPostfill = needsPostfill;
        this.followUpFormBehaviour = followUpFormBehaviour;
        this.followUpFormHints = followUpFormHints;
    }

    public boolean needsPostfill() {
        return needsPostfill;
    }

    public FormBehaviour getFollowUpFormBehaviour() {
        return followUpFormBehaviour;
    }

    public Map<String, String> getFollowUpFormHints() {
        return followUpFormHints;
    }


}
