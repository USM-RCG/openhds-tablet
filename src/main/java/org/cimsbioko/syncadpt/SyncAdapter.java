package org.cimsbioko.syncadpt;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import org.cimsbioko.R;
import org.cimsbioko.sidecar.Sidecar;
import org.cimsbioko.sidecar.SidecarNotFoundException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.cimsbioko.utilities.ConfigUtils.getPreferenceBool;
import static org.cimsbioko.utilities.NetUtils.isWiFiConnected;
import static org.cimsbioko.utilities.SyncUtils.downloadUpdate;
import static org.cimsbioko.utilities.SyncUtils.getSyncEndpoint;


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

        if (wifiOnlyEnabled && !isWiFiConnected()) {
            Log.w(TAG, "user settings require wi-fi, not syncing");
            return;
        }

        if (autoSyncEnabled || manualSyncRequested) {
            try {
                if (wifiOnlyEnabled && sidecarEnabled) {
                    NsdServiceInfo info = Sidecar.discover((NsdManager) ctx.getSystemService(Context.NSD_SERVICE), 30);
                    URL endpoint = new URL("http", info.getHost().getHostName(), info.getPort(), ctx.getString(R.string.sync_database_path));
                    Log.i(TAG, "local sync " + endpoint);
                    downloadUpdate(ctx, endpoint, null);
                } else {
                    URL endpoint = getSyncEndpoint(ctx);
                    Log.i(TAG, "remote sync " + endpoint);
                    String token = AccountManager.get(ctx).blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE_DEVICE, true);
                    downloadUpdate(ctx, endpoint, token);
                }
            } catch (MalformedURLException e) {
                Log.w(TAG, "bad endpoint url", e);
            } catch (SidecarNotFoundException | AuthenticatorException | IOException | OperationCanceledException e) {
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
