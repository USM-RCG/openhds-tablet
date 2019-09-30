package org.cimsbioko.utilities;

import android.accounts.*;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.cimsbioko.App;
import org.cimsbioko.R;
import org.cimsbioko.provider.FormsProviderAPI;
import org.cimsbioko.task.http.HttpTask;
import org.cimsbioko.task.http.HttpTaskRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static org.cimsbioko.syncadpt.Constants.ACCOUNT_TYPE;
import static org.cimsbioko.syncadpt.Constants.AUTHTOKEN_TYPE_DEVICE;
import static org.cimsbioko.utilities.HttpUtils.encodeBearerCreds;
import static org.cimsbioko.utilities.IOUtils.close;
import static org.cimsbioko.utilities.LoginUtils.launchLogin;
import static org.cimsbioko.utilities.SyncUtils.downloadedContentBefore;
import static org.cimsbioko.utilities.UrlUtils.buildServerUrl;

public class SetupUtils {

    private static final String[] REQUIRED_PERMISSIONS = new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    public static final String CAMPAIGN_DOWNLOADED_ACTION = "CAMPAIGN_DOWNLOADED";
    public static final String CAMPAIGN_FILENAME = "campaign.zip";

    public static boolean setupRequirementsMet(Context ctx) {
        return hasRequiredPermissions(ctx)
                && isFormsAppInstalled(ctx.getPackageManager())
                && isAccountInstalled(ctx)
                && isConfigAvailable()
                && isDataAvailable(ctx);
    }

    public static boolean hasRequiredPermissions(Context ctx) {
        for (String perm : REQUIRED_PERMISSIONS) {
            if (checkSelfPermission(ctx, perm) != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void startApp(final Activity source) {
        createNotificationChannels(source);
        launchLogin(source);
    }

    private static void createNotificationChannels(Context ctx) {
        NotificationUtils.createChannels(ctx.getApplicationContext());
    }

    public static void askForPermissions(Activity activity, int requestCode) {
        if (needsPermissions(activity)) {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, requestCode);
        }
    }

    private static boolean needsPermissions(Context ctx) {
        return !hasRequiredPermissions(ctx);
    }

    public static boolean isConfigAvailable() {
        return getCampaignFile().canRead();
    }

    private static File getCampaignFile() {
        return App.getApp().getFileStreamPath(CAMPAIGN_FILENAME);
    }

    public static boolean isDataAvailable(Context ctx) {
        return downloadedContentBefore(ctx);
    }

    public static boolean isAccountInstalled(Context ctx) {
        return AccountManager.get(ctx).getAccountsByType(ACCOUNT_TYPE).length > 0;
    }

    public static boolean isFormsAppInstalled(PackageManager manager) {
        Intent formsIntent = new Intent(Intent.ACTION_EDIT, FormsProviderAPI.FormsColumns.CONTENT_URI);
        List<ResolveInfo> intentMatches = manager.queryIntentActivities(formsIntent, 0);
        return !intentMatches.isEmpty();
    }

    public static void promptFormsAppInstall(final Activity activity) {

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    launchFormsAppMarketInstall();
                }
                activity.finish();
            }

            private void launchFormsAppMarketInstall() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=org.cimsbioko.forms"));
                activity.startActivity(intent);
            }
        };

        DialogInterface.OnCancelListener cancelListener = dialog -> activity.finish();

        new AlertDialog.Builder(activity)
                .setTitle(R.string.forms_app_required)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.forms_app_install_prompt)
                .setNegativeButton(R.string.quit_label, clickListener)
                .setPositiveButton(R.string.install_label, clickListener)
                .setOnCancelListener(cancelListener)
                .show();
    }

    public static void getToken(final Activity activity, AccountManagerCallback<Bundle> callback) {
        AccountManager
                .get(activity.getApplicationContext())
                .getAuthTokenByFeatures(ACCOUNT_TYPE, AUTHTOKEN_TYPE_DEVICE, null, activity, null, null, callback, null);
    }

    public static void downloadConfig(final Activity activity) {

        getToken(activity, future -> {
            String token;
            Context ctx = activity.getApplicationContext();
            String url = buildServerUrl(ctx, "/api/rest/campaign");

            // Extract the auth token for the active account
            try {
                Bundle bundle = future.getResult();
                token = bundle.getString(KEY_AUTHTOKEN);
            } catch (AuthenticatorException | IOException | OperationCanceledException e) {
                MessageUtils.showLongToast(activity, "Failed to get auth token: " + e.getMessage());
                return;
            }

            // Download the campaign file and send a local broadcast message when it finishes
            new HttpTask(rsp -> {
                if (rsp.isSuccess()) {
                    close(rsp.getInputStream());
                    LocalBroadcastManager
                            .getInstance(ctx)
                            .sendBroadcast(new Intent(CAMPAIGN_DOWNLOADED_ACTION));
                } else {
                    MessageUtils.showLongToast(ctx, "Download failed: " + rsp.getResult());
                }
            }).execute(new HttpTaskRequest(url, null, encodeBearerCreds(token), null, getCampaignFile()));
        });
    }
}
