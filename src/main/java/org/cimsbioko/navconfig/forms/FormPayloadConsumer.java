package org.cimsbioko.navconfig.forms;

import java.util.Map;

public interface FormPayloadConsumer {

    ConsumerResult consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx);

    void augmentInstancePayload(Map<String, String> formPayload);

}
