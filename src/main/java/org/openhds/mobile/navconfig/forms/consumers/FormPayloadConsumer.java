package org.openhds.mobile.navconfig.forms.consumers;

import org.openhds.mobile.navconfig.forms.LaunchContext;

import java.util.Map;

public interface FormPayloadConsumer {

    ConsumerResult consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx);

    void augmentInstancePayload(Map<String, String> formPayload);

}
