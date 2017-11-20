package org.openhds.mobile.navconfig.forms.builders;

import org.openhds.mobile.navconfig.forms.LaunchContext;

import java.util.Map;

public interface FormPayloadBuilder {
    Map<String, String> buildPayload(LaunchContext ctx);
}

