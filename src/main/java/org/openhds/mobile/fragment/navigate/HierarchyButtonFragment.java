package org.openhds.mobile.fragment.navigate;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

import java.util.HashMap;
import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.HierarchyNavigator;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

public class HierarchyButtonFragment extends Fragment {

	// for some reason margin in layout XML is ignored
	private static final int BUTTON_MARGIN = 10;

	private HierarchyNavigator navigator;
	private Map<String, RelativeLayout> viewsForStates;
    private int hiearchySelectionDrawableId;


    /*TODO: take in an object with references to SELECTED and NON-SELECTED drawables and point to them in
    * TODO: setButtonHighligted();
    */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout selectionContainer = (LinearLayout) inflater.inflate(
				R.layout.hierarchy_button_fragment, container, false);

		viewsForStates = new HashMap<String, RelativeLayout>();
		HierarchyButtonListener listener = new HierarchyButtonListener();

		Map<String, Integer> labels = navigator.getStateLabels();
		for (String state : navigator.getStateSequence()) {
			final String description = null;
			RelativeLayout layout = makeTextWithPayload(getActivity(),
                    getResourceString(getActivity(), labels.get(state)), description, state, listener,
                    selectionContainer, hiearchySelectionDrawableId, null, null,true);
			LayoutParams params = (LayoutParams) layout.getLayoutParams();
			params.setMargins(0, 0, 0, BUTTON_MARGIN);

			viewsForStates.put(state, layout);
			setButtonAllowed(state, false);
			setButtonHighlighted(state, false);
		}

		return selectionContainer;
	}

	public void setNavigator(HierarchyNavigator navigator) {
		this.navigator = navigator;
	}

	public void setButtonAllowed(String state, boolean isShown) {

		RelativeLayout layout = viewsForStates.get(state);

		if (null == layout) {
			return;
		}
		layout.setVisibility(isShown ? View.VISIBLE : View.INVISIBLE);
	}

	public void setButtonLabel(String state, String name, String id, boolean centerText) {
		RelativeLayout layout = viewsForStates.get(state);
		if (null == layout) {
			return;
		}
		configureTextWithPayload(getActivity(), layout, name, id, null, null, centerText);
	}

    public void setHiearchySelectionDrawableId(int hiearchySelectionDrawableId) {
        this.hiearchySelectionDrawableId = hiearchySelectionDrawableId;
    }

    private class HierarchyButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			navigator.jumpUp((String) v.getTag());

		}
	}

	public void setButtonHighlighted(String state, boolean isHighlighted) {

		RelativeLayout layout = viewsForStates.get(state);

		if (null == layout) {
			return;
		}

        layout.setPressed(isHighlighted);
        layout.setClickable(!isHighlighted);

	}
}
