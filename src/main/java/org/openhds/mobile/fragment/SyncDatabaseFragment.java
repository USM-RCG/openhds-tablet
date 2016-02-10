package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.OpeningActivity;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openhds.mobile.provider.OpenHDSProvider.DATABASE_NAME;
import static org.openhds.mobile.provider.OpenHDSProvider.getDatabaseHelper;
import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.SyncUtils.SQLITE_MIME_TYPE;
import static org.openhds.mobile.utilities.SyncUtils.hashFilename;
import static org.openhds.mobile.utilities.SyncUtils.loadHash;
import static org.openhds.mobile.utilities.SyncUtils.storeHash;
import static org.openhds.mobile.utilities.SyncUtils.tempFilename;

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

        Button updateButton = (Button) view.findViewById(R.id.sync_update_button);
        updateButton.setOnClickListener(new UpdateListener());

        updateStatus();

        return view;
    }

    private void updateStatus() {
        String fpVal = getFingerprint();
        fingerprint.setText(fpVal.length() > 8 ? fpVal.substring(0, 8) + '\u2026' : fpVal);
        lastUpdated.setText(getLastUpdated());
    }

    private File getDatabaseFile() {
        return getActivity().getDatabasePath(DATABASE_NAME);
    }

    private File getDatabaseTempFile() {
        return getActivity().getDatabasePath(tempFilename(DATABASE_NAME));
    }

    private File getFingerprintFile() {
        return getActivity().getDatabasePath(hashFilename(DATABASE_NAME));
    }

    public String getFingerprint() {
        String content = loadHash(getFingerprintFile());
        return content != null ? content : "-";
    }

    public CharSequence getLastUpdated() {
        File f = getFingerprintFile();
        return f.exists() ? getRelativeTimeSpanString(getActivity(), f.lastModified(), false) : "Never";
    }


    /**
     * Builds a sync request for efficiently updating local database content.
     */
    private HttpTaskRequest buildHttpRequest() throws MalformedURLException {
        Bundle extras = getActivity().getIntent().getExtras();
        String username = (String) extras.get(OpeningActivity.USERNAME_KEY);
        String password = (String) extras.get(OpeningActivity.PASSWORD_KEY);
        return new HttpTaskRequest(getSyncEndpoint().toExternalForm(), SQLITE_MIME_TYPE, username, password,
                getFingerprint(), getDatabaseTempFile());
    }

    private URL getSyncEndpoint() throws MalformedURLException {
        String baseUrl = getPreferenceString(getActivity(), R.string.openhds_server_url_key, "");
        String path = getResourceString(getActivity(), R.string.sync_database_path);
        return new URL(baseUrl + path);
    }


    private class UpdateListener implements View.OnClickListener, HttpTask.HttpTaskResponseHandler {

        @Override
        public void onClick(View v) {
            HttpTask httpTask = new HttpTask(this);
            try {
                HttpTaskRequest httpReq = buildHttpRequest();
                httpTask.execute(httpReq);
            } catch (MalformedURLException e) {
                showLongToast(getActivity(), R.string.url_error);
            }
        }

        @Override
        public void handleHttpTaskResponse(HttpTaskResponse response) {
            if (response.getHttpStatus() == SC_NOT_MODIFIED) {
                showLongToast(getActivity(), R.string.sync_state_noupdate);
            } else if (response.getHttpStatus() == SC_OK) {
                File dbTmpFile = getDatabaseTempFile(), dbFile = getDatabaseFile();
                if (!dbTmpFile.exists()) {
                    showLongToast(getActivity(), "Database download failed");
                } else {
                    getDatabaseHelper(getActivity()).close();
                    if (!dbTmpFile.renameTo(dbFile)) {
                        showLongToast(getActivity(), "Database rename failed");
                    } else {
                        storeHash(getFingerprintFile(), response.getETag());
                        showLongToast(getActivity(), "Database updated");
                    }
                }
            } else {
                showLongToast(getActivity(), "Sync failed: " + response.getMessage());
            }
            updateStatus();
        }
    }
}
