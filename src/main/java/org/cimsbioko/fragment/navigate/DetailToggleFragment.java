package org.cimsbioko.fragment.navigate;

import static org.cimsbioko.utilities.LayoutUtils.configureTextWithPayload;
import static org.cimsbioko.utilities.LayoutUtils.makeTextWithPayload;

import org.cimsbioko.R;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

public class DetailToggleFragment extends Fragment implements OnClickListener {

	private static final int BUTTON_MARGIN = 5;

	private RelativeLayout layout;
	private boolean isEnabled;
	private DetailToggleListener listener;

	public interface DetailToggleListener {
		void onDetailToggled();
	}

	@Override
	public void onAttach(Context ctx) {
		super.onAttach(ctx);
		if (ctx instanceof DetailToggleListener) {
			listener = (DetailToggleListener)ctx;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout toggleContainer = (LinearLayout) inflater.inflate(R.layout.detail_toggle_fragment, container, false);
		layout = makeTextWithPayload(getActivity(), null, null, null, this, toggleContainer, 0, null, null,true);
		LayoutParams params = (LayoutParams) layout.getLayoutParams();
		params.setMargins(0, 0, 0, BUTTON_MARGIN);
		return toggleContainer;
	}

	@Override
	public void onClick(View v) {
		if (listener != null) {
			listener.onDetailToggled();
		}
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
		if (layout != null) {
			if (!isEnabled) {
				layout.setVisibility(ViewGroup.INVISIBLE);
			} else {
				layout.setVisibility(ViewGroup.VISIBLE);
				layout.setClickable(true);
				setHighlighted(false);
			}
		}
	}

	public void setHighlighted(boolean isHighlighted) {
		if (layout != null) {
			if (isEnabled && isHighlighted) {
				layout.setBackgroundColor(getResources().getColor(R.color.LightGreen));
				configureTextWithPayload(getActivity(), layout,
						getString(R.string.toggle_fragment_button_show_children), null, null, null, true);
			} else if (isEnabled && !isHighlighted) {
				layout.setBackgroundColor(getResources().getColor(R.color.DarkGreen));
				configureTextWithPayload(getActivity(), layout,
						getString(R.string.toggle_fragment_button_show_details), null, null, null, true);
			}
		}
	}

}
