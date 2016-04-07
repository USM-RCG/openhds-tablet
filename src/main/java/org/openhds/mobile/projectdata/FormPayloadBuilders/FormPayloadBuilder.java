package org.openhds.mobile.projectdata.FormPayloadBuilders;

import java.util.Map;

public interface FormPayloadBuilder {
	Map<String, String> buildPayload(LaunchContext ctx);
}

