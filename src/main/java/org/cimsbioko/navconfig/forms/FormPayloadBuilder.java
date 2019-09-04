package org.cimsbioko.navconfig.forms;

import java.util.Map;

public interface FormPayloadBuilder {
    Map<String, String> buildPayload(LaunchContext ctx);
}

