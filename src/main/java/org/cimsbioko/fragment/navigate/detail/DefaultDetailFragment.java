package org.cimsbioko.fragment.navigate.detail;

import org.cimsbioko.R;
import org.cimsbioko.data.DataWrapper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DefaultDetailFragment extends DetailFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.default_detail_fragment, container, false);
	}

	@Override
	public void setUpDetails(DataWrapper data) {
	}
}
