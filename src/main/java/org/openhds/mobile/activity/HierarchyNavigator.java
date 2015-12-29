package org.openhds.mobile.activity;

import java.util.List;
import java.util.Map;

import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.model.form.FormBehaviour;

public interface HierarchyNavigator {

	public Map<String, Integer> getStateLabels();

	public List<String> getStateSequence();

	public void jumpUp(String state);

	public void stepDown(DataWrapper qr);
	
	public void launchForm(FormBehaviour form, Map<String, String> followUpformHints);
}
