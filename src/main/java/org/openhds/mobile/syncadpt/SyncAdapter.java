package org.openhds.mobile.syncadpt;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.LoginActivity;
import org.openhds.mobile.utilities.SyncUtils;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static org.openhds.mobile.utilities.SyncUtils.downloadUpdate;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        Context ctx = getContext();
        downloadUpdate(ctx, account.name, AccountManager.get(ctx).getPassword(account));
    }
}
