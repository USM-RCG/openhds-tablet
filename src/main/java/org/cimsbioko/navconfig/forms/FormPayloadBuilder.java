package org.cimsbioko.navconfig.forms;

import org.cimsbioko.navconfig.forms.LaunchContext;

import java.util.Map;

public interface FormPayloadBuilder {
    Map<String, String> buildPayload(LaunchContext ctx);
}

