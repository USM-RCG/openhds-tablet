package org.openhds.mobile.navconfig.forms.consumers;

import org.openhds.mobile.navconfig.forms.LaunchContext;

import java.util.Map;

public interface FormPayloadConsumer {

    ConsumerResults consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx);

    void postFillFormPayload(Map<String, String> formPayload);

}
