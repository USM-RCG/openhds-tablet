package org.cimsbioko.fragment.navigate.detail;

import androidx.fragment.app.Fragment;

import org.cimsbioko.repository.DataWrapper;

public abstract class DetailFragment extends Fragment {

    public abstract void setUpDetails(DataWrapper data);

}
