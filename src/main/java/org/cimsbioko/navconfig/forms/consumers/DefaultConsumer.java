package org.cimsbioko.navconfig.forms.consumers;

import org.cimsbioko.navconfig.forms.LaunchContext;

import java.util.Map;

/**
 * Default form consumer implementation. This can be used as an adapter supplying default method implementations, but it
 * also serves as a Null Object for consumers.
 */
public class DefaultConsumer implements FormPayloadConsumer {

    @Override
    public ConsumerResult consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {
        return new ConsumerResult(false);
    }

    @Override
    public void augmentInstancePayload(Map<String, String> formPayload) {
    }
}
