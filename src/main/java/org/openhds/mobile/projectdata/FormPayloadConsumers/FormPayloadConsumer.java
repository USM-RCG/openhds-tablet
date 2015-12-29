package org.openhds.mobile.projectdata.FormPayloadConsumers;

import java.util.Map;

import org.openhds.mobile.activity.NavigateActivity;

public interface FormPayloadConsumer {
	public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                              NavigateActivity navigateActivity);
	public void postFillFormPayload(Map<String, String> formPayload);
}
