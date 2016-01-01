package org.openhds.mobile.fragment.navigate.detail;

import org.openhds.mobile.activity.NavigateActivity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DetailFragment extends Fragment {

	protected NavigateActivity navigateActivity;

	public void setUpDetails() {
	}

	public void setNavigateActivity(NavigateActivity navigateActivity) {
		this.navigateActivity = navigateActivity;
	}

}
