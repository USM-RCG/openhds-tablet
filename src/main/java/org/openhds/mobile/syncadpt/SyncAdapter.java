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
import org.openhds.mobile.activity.OpeningActivity;
import org.openhds.mobile.utilities.SyncUtils;

import static org.openhds.mobile.utilities.SyncUtils.downloadUpdate;


public class SyncAdapter extends AbstractThreadedSyncAdapter implements SyncUtils.DatabaseUpdateListener {

    private static final String TAG = SyncAdapter.class.getName();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        Context ctx = getContext();
        String username = account.name, password = AccountManager.get(ctx).getPassword(account);
        downloadUpdate(ctx, username, password, this);
    }

    @Override
    public void downloadedUpdate() {
        Context ctx = getContext();
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(ctx, OpeningActivity.class);
        PendingIntent pending = PendingIntent.getActivity(ctx, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(ctx)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("New data available")
                .setContentText("Login as supervisor to apply the update")
                .setContentIntent(pending);
        manager.notify(42, builder.getNotification());
    }
}
