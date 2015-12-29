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

public class VisitFragment extends Fragment implements OnClickListener {
	NavigateActivity navigateActivity;

	private static final int BUTTON_MARGIN = 10;

	RelativeLayout layout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		LinearLayout toggleContainer = (LinearLayout) inflater.inflate(
				R.layout.visit_fragment, container, false);

		layout = makeTextWithPayload(getActivity(), null, null, null, this,
                toggleContainer, 0, null, null, true);
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
		navigateActivity.finishVisit();
	}

	public void setButtonEnabled(boolean isEnabled) {

		if (null == layout) {
			return;
		}
		if (isEnabled) {
			layout.setVisibility(ViewGroup.VISIBLE);
			layout.setBackgroundResource(R.drawable.visit_selector);
			configureTextWithPayload(getActivity(), layout, getResources()
                            .getString(R.string.finish_visit), null,
                    null, null,true );
			layout.setClickable(true);

		} else {
			layout.setVisibility(ViewGroup.INVISIBLE);
		}
	}
}
