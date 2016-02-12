package org.openhds.mobile.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openhds.mobile.provider.OpenHDSProvider.getDatabaseHelper;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.SyncUtils.buildHttpRequest;
import static org.openhds.mobile.utilities.SyncUtils.getDatabaseFile;
import static org.openhds.mobile.utilities.SyncUtils.getDatabaseTempFile;
import static org.openhds.mobile.utilities.SyncUtils.getFingerprint;
import static org.openhds.mobile.utilities.SyncUtils.getFingerprintFile;
import static org.openhds.mobile.utilities.SyncUtils.storeHash;

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

    public CharSequence getLastUpdated() {
        File f = getFingerprintFile(getActivity());
        return f.exists() ? getRelativeTimeSpanString(getActivity(), f.lastModified(), false) : "Never";
    }

    private void updateStatus() {
        String fpVal = getFingerprint(getActivity());
        fingerprint.setText(fpVal.length() > 8 ? fpVal.substring(0, 8) + '\u2026' : fpVal);
        lastUpdated.setText(getLastUpdated());
    }

    private class UpdateListener implements View.OnClickListener, HttpTask.HttpTaskResponseHandler {

        @Override
        public void onClick(View v) {
            HttpTask httpTask = new HttpTask(this);
            try {
                Activity activity = getActivity();
                Bundle extras = activity.getIntent().getExtras();
                String username = (String) extras.get(OpeningActivity.USERNAME_KEY);
                String password = (String) extras.get(OpeningActivity.PASSWORD_KEY);
                HttpTaskRequest httpReq = buildHttpRequest(activity, username, password);
                httpTask.execute(httpReq);
            } catch (MalformedURLException e) {
                showLongToast(getActivity(), R.string.url_error);
            }
        }

        @Override
        public void handleHttpTaskResponse(HttpTaskResponse response) {
            Context ctx = getActivity();
            if (response.getHttpStatus() == SC_NOT_MODIFIED) {
                showLongToast(ctx, R.string.sync_state_noupdate);
            } else if (response.getHttpStatus() == SC_OK) {
                File dbTmpFile = getDatabaseTempFile(ctx), dbFile = getDatabaseFile(ctx);
                if (!dbTmpFile.exists()) {
                    showLongToast(ctx, "Database download failed");
                } else {
                    getDatabaseHelper(ctx).close();
                    if (!dbTmpFile.renameTo(dbFile)) {
                        showLongToast(ctx, "Database rename failed");
                    } else {
                        storeHash(getFingerprintFile(ctx), response.getETag());
                        showLongToast(ctx, "Database updated");
                    }
                }
            } else {
                showLongToast(ctx, "Sync failed: " + response.getMessage());
            }
            updateStatus();
        }
    }
}
