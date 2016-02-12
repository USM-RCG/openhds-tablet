package org.openhds.mobile.syncadpt;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.openhds.mobile.R;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openhds.mobile.provider.OpenHDSProvider.getDatabaseHelper;
import static org.openhds.mobile.utilities.HttpUtils.encodeBasicCreds;
import static org.openhds.mobile.utilities.HttpUtils.get;
import static org.openhds.mobile.utilities.SyncUtils.SQLITE_MIME_TYPE;
import static org.openhds.mobile.utilities.SyncUtils.getDatabaseFile;
import static org.openhds.mobile.utilities.SyncUtils.getDatabaseTempFile;
import static org.openhds.mobile.utilities.SyncUtils.getFingerprint;
import static org.openhds.mobile.utilities.SyncUtils.getFingerprintFile;
import static org.openhds.mobile.utilities.SyncUtils.getSyncEndpoint;
import static org.openhds.mobile.utilities.SyncUtils.storeHash;
import static org.openhds.mobile.utilities.SyncUtils.streamToFile;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getName();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        Context ctx = getContext();
        try {
            String creds = encodeBasicCreds(account.name, AccountManager.get(ctx).getPassword(account));
            HttpURLConnection httpConn = get(getSyncEndpoint(ctx), SQLITE_MIME_TYPE, creds, getFingerprint(ctx));
            int result = httpConn.getResponseCode();
            switch (result) {
                case SC_NOT_MODIFIED:
                    Log.i(TAG, "no update found");
                    break;
                case SC_OK:
                    Log.i(TAG, "update found, downloading");
                    File dbTmpFile = getDatabaseTempFile(ctx), dbFile = getDatabaseFile(ctx);
                    streamToFile(httpConn.getInputStream(), getDatabaseTempFile(ctx));
                    if (!dbTmpFile.renameTo(dbFile)) {
                        Log.i(TAG, "database rename failed");
                    } else {
                        storeHash(getFingerprintFile(ctx), httpConn.getHeaderField("ETag"));
                        getDatabaseHelper(ctx).close();
                        Log.i(TAG, "database updated");
                        sendNotification(ctx);
                    }
                    break;
                default:
                    Log.i(TAG, "unexpected status code " + result);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "invalid sync endpoint", e);
        } catch (IOException e) {
            Log.e(TAG, "sync io failure", e);
        }
    }

    private void sendNotification(Context ctx) {
        Notification.Builder builder = new Notification.Builder(ctx)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Database updated")
                .setContentText("Local data was updated to latest from server");
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(42, builder.getNotification());
    }
}
