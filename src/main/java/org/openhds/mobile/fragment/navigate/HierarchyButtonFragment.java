package org.openhds.mobile.fragment.navigate;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.HierarchyPath;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.repository.DataWrapper;

import java.util.HashMap;
import java.util.Map;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

public class HierarchyButtonFragment extends Fragment implements OnClickListener {

	// for some reason margin in layout XML is ignored
	private static final int BUTTON_MARGIN = 5;

	private HierarchyButtonListener listener;
	private Map<String, RelativeLayout> levelViews;
	private NavigatorConfig config;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		config = NavigatorConfig.getInstance();
	}

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

		levelViews = new HashMap<>();

		NavigatorConfig config = NavigatorConfig.getInstance();
		for (String level : config.getLevels()) {
			final String description = null;
			RelativeLayout layout = makeTextWithPayload(getActivity(),
                    getResourceString(getActivity(), config.getLevelLabel(level)), description, level, this,
					fragmentLayout, R.drawable.data_selector, null, null,true);
			LayoutParams params = (LayoutParams) layout.getLayoutParams();
			params.setMargins(0, 0, 0, BUTTON_MARGIN);
			levelViews.put(level, layout);
			setVisible(level, false);
			setHighlighted(level, false);
		}

		return fragmentLayout;
	}

	public void update(HierarchyPath path) {

		// Configure and show all hierarchy buttons for levels in path
		for (String lvl : path.getLevels()) {
			updateButton(lvl, path.get(lvl));
			setVisible(lvl, true);
		}

		// Hide all buttons not in path
		for (int i = path.depth(); i < config.getLevels().size(); i++) {
			setVisible(config.getLevels().get(i), false);
		}

		// If we can go deeper, enable the next level (disabled with the level name)
		if (path.depth() < config.getLevels().size()) {
			String nextLevel = config.getLevels().get(path.depth());
			updateButton(nextLevel, path.get(nextLevel));
			setVisible(nextLevel, true);
		}
	}

	private void updateButton(String level, DataWrapper data) {
		if (data == null) {
			String levelLabel = getString(config.getLevelLabel(level));
			setButtonLabel(level, levelLabel, null, true);
			setHighlighted(level, true);
		} else {
			setButtonLabel(level, data.getName(), data.getExtId(), false);
			setHighlighted(level, false);
		}
	}

	private void setVisible(String level, boolean visible) {
		RelativeLayout layout = levelViews.get(level);
		if (layout != null) {
			layout.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		}
	}

	private void setHighlighted(String level, final boolean highlighted) {
		final RelativeLayout layout = levelViews.get(level);
		if (layout != null) {
			// Defer setting pressed state so it isn't overwritten when run within click handler
			Handler h = new Handler(Looper.getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					layout.setClickable(!highlighted);
					layout.setPressed(highlighted);
				}
			});
		}
	}

	private void setButtonLabel(String level, String name, String id, boolean centerText) {
		RelativeLayout layout = levelViews.get(level);
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
