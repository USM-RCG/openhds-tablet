package org.openhds.mobile.projectdata.FormPayloadConsumers;

import java.util.Map;

import org.openhds.mobile.activity.NavigateActivity;

public interface FormPayloadConsumer {
	ConsumerResults consumeFormPayload(Map<String, String> formPayload,
									   NavigateActivity navigateActivity);
	void postFillFormPayload(Map<String, String> formPayload);
}
