package org.openhds.mobile.fragment.navigate;

import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.NavigateActivity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

public class DetailToggleFragment extends Fragment implements OnClickListener {

	NavigateActivity navigateActivity;

	private static final int BUTTON_MARGIN = 10;

	RelativeLayout layout;

	private boolean isEnabled;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		LinearLayout toggleContainer = (LinearLayout) inflater.inflate(
				R.layout.detail_toggle_fragment, container, false);

		layout = makeTextWithPayload(getActivity(), null, null, null, this,
                toggleContainer, 0, null, null,true);
		LayoutParams params = (LayoutParams) layout.getLayoutParams();
		params.setMargins(0, 0, 0, BUTTON_MARGIN);

		return toggleContainer;
	}

	public void setNavigateActivity(NavigateActivity navigateActivity) {
		this.navigateActivity = navigateActivity;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		navigateActivity.toggleMiddleFragment();
	}

	public void setButtonEnabled(boolean isEnabled) {

		this.isEnabled = isEnabled;

		if (null == layout) {
			return;
		}
		if (!isEnabled) {
			// use this if you want it to be grayed out instead of invisible
			//
			// layout.setBackgroundColor(getResources().getColor(R.color.Gray));
			// configureTextWithPayload(getActivity(), layout, getResources()
			// .getString(R.string.toggle_fragment_button_none), null,
			// null, null);
			// layout.setClickable(false);

			layout.setVisibility(ViewGroup.INVISIBLE);
		} else {
			layout.setVisibility(ViewGroup.VISIBLE);
			layout.setClickable(true);
			setButtonHighlighted(false);
		}

	}

	public void setButtonHighlighted(boolean isHighlighted) {

		if (null == layout) {
			return;
		}

		if (isEnabled && isHighlighted) {
			layout.setBackgroundColor(getResources().getColor(
					R.color.LightGreen));

			configureTextWithPayload(getActivity(), layout, getResources()
                            .getString(R.string.toggle_fragment_button_show_children),
                    null, null, null,true);

		} else if (isEnabled && !isHighlighted) {
			layout.setBackgroundColor(getResources().getColor(
					R.color.DarkSeaGreen));

			configureTextWithPayload(getActivity(), layout, getResources()
                            .getString(R.string.toggle_fragment_button_show_details),
                    null, null, null,true);

		}

	}

}
