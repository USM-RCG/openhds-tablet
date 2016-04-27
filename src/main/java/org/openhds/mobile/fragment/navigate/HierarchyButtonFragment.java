package org.openhds.mobile.fragment.navigate;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.HierarchyNavigator;

import java.util.HashMap;
import java.util.Map;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

public class HierarchyButtonFragment extends Fragment {

	// for some reason margin in layout XML is ignored
	private static final int BUTTON_MARGIN = 5;

	private HierarchyNavigator navigator;
	private Map<String, RelativeLayout> stateViews;
    private int buttonDrawable;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		LinearLayout selectionContainer = (LinearLayout) inflater.inflate(
				R.layout.hierarchy_button_fragment, container, false);

		stateViews = new HashMap<>();
		HierarchyButtonListener listener = new HierarchyButtonListener();

		Map<String, Integer> labels = navigator.getLevelLabels();
		for (String state : navigator.getLevels()) {
			final String description = null;
			RelativeLayout layout = makeTextWithPayload(getActivity(),
                    getResourceString(getActivity(), labels.get(state)), description, state, listener,
                    selectionContainer, buttonDrawable, null, null,true);
			LayoutParams params = (LayoutParams) layout.getLayoutParams();
			params.setMargins(0, 0, 0, BUTTON_MARGIN);
			stateViews.put(state, layout);
			setVisible(state, false);
			setHighlighted(state, false);
		}

		return selectionContainer;
	}

	public void setNavigator(HierarchyNavigator navigator) {
		this.navigator = navigator;
	}

	public void setVisible(String state, boolean visible) {
		RelativeLayout layout = stateViews.get(state);
		if (layout != null) {
			layout.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		}
	}

	public void setHighlighted(String state, boolean highlighted) {
		RelativeLayout layout = stateViews.get(state);
		if (layout != null) {
			layout.setPressed(highlighted);
			layout.setClickable(!highlighted);
		}
	}

	public void setButtonLabel(String state, String name, String id, boolean centerText) {
		RelativeLayout layout = stateViews.get(state);
		if (layout != null) {
			configureTextWithPayload(getActivity(), layout, name, id, null, null, centerText);
		}
	}

    public void setButtonDrawable(int drawable) {
        this.buttonDrawable = drawable;
    }

    private class HierarchyButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			navigator.jumpUp((String) v.getTag());
		}
	}
}
