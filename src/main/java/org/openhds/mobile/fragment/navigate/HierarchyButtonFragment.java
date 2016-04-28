package org.openhds.mobile.fragment.navigate;

import android.app.Activity;
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
import org.openhds.mobile.navconfig.HierarchyInfo;
import org.openhds.mobile.navconfig.NavigatorConfig;

import java.util.HashMap;
import java.util.Map;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

public class HierarchyButtonFragment extends Fragment implements OnClickListener {

	// for some reason margin in layout XML is ignored
	private static final int BUTTON_MARGIN = 5;

	private HierarchyButtonListener listener;
	private Map<String, RelativeLayout> stateViews;

	public interface HierarchyButtonListener {
		void onHierarchyButtonClicked(String level);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof HierarchyButtonListener) {
			listener = (HierarchyButtonListener)activity;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		LinearLayout fragmentLayout = (LinearLayout) inflater.inflate(
				R.layout.hierarchy_button_fragment, container, false);

		stateViews = new HashMap<>();

		NavigatorConfig config = NavigatorConfig.getInstance();
		for (String level : config.getLevels()) {
			final String description = null;
			RelativeLayout layout = makeTextWithPayload(getActivity(),
                    getResourceString(getActivity(), config.getLevelLabel(level)), description, level, this,
					fragmentLayout, R.drawable.data_selector, null, null,true);
			LayoutParams params = (LayoutParams) layout.getLayoutParams();
			params.setMargins(0, 0, 0, BUTTON_MARGIN);
			stateViews.put(level, layout);
			setVisible(level, false);
			setHighlighted(level, false);
		}

		return fragmentLayout;
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

	@Override
	public void onClick(View v) {
		if (listener != null) {
			listener.onHierarchyButtonClicked((String) v.getTag());
		}
	}
}
