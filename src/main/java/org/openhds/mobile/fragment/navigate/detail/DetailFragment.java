package org.openhds.mobile.fragment.navigate.detail;

import android.support.v4.app.Fragment;

import org.openhds.mobile.repository.DataWrapper;

public abstract class DetailFragment extends Fragment {

    public abstract void setUpDetails(DataWrapper data);

}
