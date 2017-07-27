package org.openhds.mobile.syncadpt;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.openhds.mobile.R;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceBool;
import static org.openhds.mobile.utilities.SyncUtils.downloadUpdate;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        Context ctx = getContext();
        boolean autoSyncEnabled = getPreferenceBool(ctx, ctx.getString(R.string.use_auto_sync_key), true);
        boolean manualSyncRequested = extras.getBoolean(ctx.getString(R.string.manual_sync_key), false);
        if (autoSyncEnabled || manualSyncRequested) {
            downloadUpdate(ctx, account.name, AccountManager.get(ctx).getPassword(account));
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
