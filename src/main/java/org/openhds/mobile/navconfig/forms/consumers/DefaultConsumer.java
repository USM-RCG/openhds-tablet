package org.openhds.mobile.navconfig.forms.consumers;

import org.openhds.mobile.navconfig.forms.LaunchContext;

import java.util.Map;

/**
 * Default form consumer implementation. This can be used as an adapter supplying default method implementations, but it
 * also serves as a Null Object for consumers.
 */
public class DefaultConsumer implements FormPayloadConsumer {

    public static final DefaultConsumer INSTANCE = new DefaultConsumer();

    @Override
    public ConsumerResults consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {
        return new ConsumerResults(false, null, null);
    }

    @Override
    public void augmentInstancePayload(Map<String, String> formPayload) {
    }
}
