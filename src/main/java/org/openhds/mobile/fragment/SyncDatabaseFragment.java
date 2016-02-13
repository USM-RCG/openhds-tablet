package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openhds.mobile.R;

import java.io.File;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.openhds.mobile.utilities.SyncUtils.getFingerprint;
import static org.openhds.mobile.utilities.SyncUtils.getFingerprintFile;

/**
 * Allow user to check for db updates, download and apply them.
 */
public class SyncDatabaseFragment extends Fragment {

    private TextView lastUpdated;
    private TextView fingerprint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sync_database_fragment, container, false);
        lastUpdated = (TextView) view.findViewById(R.id.sync_updated_column);
        fingerprint = (TextView) view.findViewById(R.id.sync_fingerprint_column);
        updateStatus();
        return view;
    }

    public CharSequence getLastUpdated() {
        File f = getFingerprintFile(getActivity());
        return f.exists() ? getRelativeTimeSpanString(getActivity(), f.lastModified(), false) : "Never";
    }

    private void updateStatus() {
        String fpVal = getFingerprint(getActivity());
        fingerprint.setText(fpVal.length() > 8 ? fpVal.substring(0, 8) + '\u2026' : fpVal);
        lastUpdated.setText(getLastUpdated());
    }
}
