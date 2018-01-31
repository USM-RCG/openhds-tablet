package org.openhds.mobile.syncadpt;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;

import org.openhds.mobile.R;
import org.openhds.mobile.sidecar.Sidecar;
import org.openhds.mobile.sidecar.SidecarNotFoundException;

import java.net.MalformedURLException;
import java.net.URL;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceBool;
import static org.openhds.mobile.utilities.SyncUtils.downloadUpdate;
import static org.openhds.mobile.utilities.SyncUtils.getSyncEndpoint;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        final Context ctx = getContext();

        boolean autoSyncEnabled = getPreferenceBool(ctx, ctx.getString(R.string.use_auto_sync_key), true);
        boolean manualSyncRequested = extras.getBoolean(ctx.getString(R.string.manual_sync_key), false);
        boolean sidecarEnabled = getPreferenceBool(ctx, ctx.getString(R.string.use_sidecar_key), false);
        boolean wifiOnlyEnabled = getPreferenceBool(ctx, ctx.getString(R.string.wifi_sync_key), true);

        if (wifiOnlyEnabled) {
            ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMgr == null || !connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                Log.w(TAG, "user settings require wi-fi, not syncing");
                return;
            }
        }

        if (autoSyncEnabled || manualSyncRequested) {
            try {
                if (sidecarEnabled) {
                    NsdServiceInfo info = Sidecar.discover((NsdManager) ctx.getSystemService(Context.NSD_SERVICE), 30);
                    URL endpoint = new URL("http", info.getHost().getHostName(), info.getPort(), ctx.getString(R.string.sync_database_path));
                    Log.i(TAG, "local sync " + endpoint);
                    downloadUpdate(ctx, endpoint, null, null);
                } else {
                    URL endpoint = getSyncEndpoint(ctx);
                    Log.i(TAG, "remote sync " + endpoint);
                    downloadUpdate(ctx, endpoint, account.name, AccountManager.get(ctx).getPassword(account));
                }
            } catch (MalformedURLException e) {
                Log.w(TAG, "bad endpoint url", e);
            } catch (SidecarNotFoundException e) {
                Log.w(TAG, e.getMessage());
            }
        } else {
            Log.d(TAG, "sync adapter ran, but ignored - auto-sync disabled by user");
        }
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        Log.i(TAG, "sync cancelled");
    }
}
