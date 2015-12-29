package org.openhds.mobile.projectdata.FormPayloadBuilders;

import java.util.Map;

import org.openhds.mobile.activity.NavigateActivity;

public interface FormPayloadBuilder {
	public void buildFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity);
}

