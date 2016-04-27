package org.openhds.mobile.fragment.navigate;

import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

import org.openhds.mobile.R;

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

public class VisitFragment extends Fragment implements OnClickListener {

	private static final int BOTTOM_MARGIN = 10;

	private RelativeLayout layout;
	private VisitFinishedListener listener;

	public interface VisitFinishedListener {
		void onVisitFinished();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof VisitFinishedListener) {
			listener = (VisitFinishedListener)activity;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout toggleContainer = (LinearLayout) inflater.inflate(R.layout.visit_fragment, container, false);
		layout = makeTextWithPayload(getActivity(), null, null, null, this, toggleContainer, 0, null, null, true);
		LayoutParams params = (LayoutParams) layout.getLayoutParams();
		params.setMargins(0, 0, 0, BOTTOM_MARGIN);
		return toggleContainer;
	}

	@Override
	public void onClick(View v) {
		if (listener != null) {
			listener.onVisitFinished();
		}
	}

	public void setEnabled(boolean enabled) {
		if (layout != null) {
			if (enabled) {
				layout.setVisibility(ViewGroup.VISIBLE);
				layout.setBackgroundResource(R.drawable.visit_selector);
				configureTextWithPayload(getActivity(), layout, getResources().getString(R.string.finish_visit), null, null, null, true);
				layout.setClickable(true);
			} else {
				layout.setVisibility(ViewGroup.GONE);
			}
		}
	}
}
