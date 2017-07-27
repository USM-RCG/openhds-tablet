package org.openhds.mobile.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.offlinedb.OfflineDbService;
import org.openhds.mobile.utilities.MessageUtils;
import org.openhds.mobile.utilities.SyncUtils;

import java.io.File;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.openhds.mobile.search.IndexingService.queueFullReindex;
import static org.openhds.mobile.search.Utils.isAutoReindexingEnabled;
import static org.openhds.mobile.utilities.SyncUtils.SYNC_NOTIFICATION_ID;
import static org.openhds.mobile.utilities.SyncUtils.canUpdateDatabase;
import static org.openhds.mobile.utilities.SyncUtils.checkForUpdate;
import static org.openhds.mobile.utilities.SyncUtils.getDatabaseFile;
import static org.openhds.mobile.utilities.SyncUtils.getDatabaseFingerprint;
import static org.openhds.mobile.utilities.SyncUtils.installUpdate;

/**
 * Allow user to check for db updates, download and apply them.
 */
public class SyncDatabaseFragment extends Fragment implements View.OnClickListener, SyncUtils.DatabaseInstallationListener {

    private TextView lastUpdated;
    private TextView fingerprint;
    private Button checkButton;
    private Button updateButton;
    private ContentObserver observer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sync_database_fragment, container, false);
        lastUpdated = (TextView) view.findViewById(R.id.sync_updated_column);
        fingerprint = (TextView) view.findViewById(R.id.sync_fingerprint_column);
        checkButton = (Button) view.findViewById(R.id.sync_check_button);
        checkButton.setOnClickListener(this);
        updateButton = (Button) view.findViewById(R.id.sync_update_button);
        updateButton.setOnClickListener(this);
        updateStatus();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                updateStatus();
            }
        };
        activity.getContentResolver().registerContentObserver(OpenHDS.CONTENT_BASE_URI, false, observer);
        activity.startService(new Intent(activity, OfflineDbService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    public void onDestroy() {
        if (observer != null) {
            getActivity().getContentResolver().unregisterContentObserver(observer);
        }
        super.onDestroy();
    }

    private File getFingerprintFile() {
        return SyncUtils.getFingerprintFile(getDatabaseFile(getActivity()));
    }

    private CharSequence getLastUpdated() {
        Context ctx = getActivity();
        File f = getFingerprintFile();
        return f.exists() ? getRelativeTimeSpanString(ctx, f.lastModified(), false) : ctx.getString(R.string.sync_database_updated_never);
    }

    private void updateStatus() {
        Context ctx = getActivity();
        String fpVal = getDatabaseFingerprint(ctx);
        fingerprint.setText(fpVal.length() > 8 ? fpVal.substring(0, 8) + '\u2026' : fpVal);
        lastUpdated.setText(getLastUpdated());
        updateButton.setEnabled(canUpdateDatabase(ctx));
    }

    @Override
    public void onClick(View v) {
        if (v == checkButton) {
            checkForUpdate(getActivity());
        } else if (v == updateButton) {
            installUpdate(getActivity(), this);
        }
    }

    @Override
    public void installed() {
        Context ctx = getActivity();
        updateStatus();
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(SYNC_NOTIFICATION_ID);
        MessageUtils.showLongToast(ctx, ctx.getString(R.string.sync_database_updated));
        if (isAutoReindexingEnabled(ctx)) {
            queueFullReindex(ctx);
        }
    }
}
