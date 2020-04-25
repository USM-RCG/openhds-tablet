package org.cimsbioko.utilities;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.cimsbioko.App;
import org.cimsbioko.R;
import org.cimsbioko.navconfig.NavigatorConfig;
import org.cimsbioko.provider.FormsProviderAPI;
import org.cimsbioko.task.campaign.CampaignTask;

import java.io.IOException;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static org.cimsbioko.campaign.CampaignUpdateService.CIMS_CAMPAIGN_ID;
import static org.cimsbioko.syncadpt.Constants.ACCOUNT_TYPE;
import static org.cimsbioko.syncadpt.Constants.AUTHTOKEN_TYPE_DEVICE;
import static org.cimsbioko.utilities.CampaignUtils.downloadedCampaignExists;
import static org.cimsbioko.utilities.ConfigUtils.clearActiveModules;
import static org.cimsbioko.utilities.ConfigUtils.getSharedPrefs;
import static org.cimsbioko.utilities.FileUtils.getFingerprintFile;
import static org.cimsbioko.utilities.IOUtils.store;
import static org.cimsbioko.utilities.LoginUtils.launchLogin;
import static org.cimsbioko.utilities.SyncUtils.downloadedContentBefore;

public class SetupUtils {

    private static final String TAG = SetupUtils.class.getSimpleName();
    private static final String[] REQUIRED_PERMISSIONS = Build.VERSION.SDK_INT >= 19 ? new String[]{} : new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    public static final String CAMPAIGN_DOWNLOADED_ACTION = "CAMPAIGN_DOWNLOADED";
    public static final String CAMPAIGN_FILENAME = "campaign.zip";

    public static boolean setupRequirementsMet(Context ctx) {
        return hasRequiredPermissions(ctx)
                && isFormsAppInstalled(ctx.getPackageManager())
                && isAccountInstalled()
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
        launchLogin(source);
    }

    public static void createNotificationChannels(Context ctx) {
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
        return downloadedCampaignExists();
    }

    public static boolean isDataAvailable(Context ctx) {
        return downloadedContentBefore(ctx);
    }

    public static boolean isAccountInstalled() {
        return AccountUtils.getFirstAccount() != null;
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

            // Extract the auth token for the active account
            try {
                Bundle bundle = future.getResult();
                token = bundle.getString(KEY_AUTHTOKEN);
            } catch (AuthenticatorException | IOException | OperationCanceledException e) {
                MessageUtils.showLongToast(activity, "Failed to get auth token: " + e.getMessage());
                return;
            }

            // Download the campaign file and send a local broadcast message when it finishes
            new CampaignTask() {
                @Override
                protected void onPostExecute(CampaignDownloadResult campaignDownloadResult) {
                    if (campaignDownloadResult.wasError()) {
                        MessageUtils.showLongToast(ctx, campaignDownloadResult.getError());
                    } else {
                        String etag = campaignDownloadResult.getEtag(), campaign = campaignDownloadResult.getCampaign();
                        if (etag != null) {
                            store(getFingerprintFile(campaignDownloadResult.getDownloadedFile()), etag);
                        }
                        Intent intent = new Intent(CAMPAIGN_DOWNLOADED_ACTION);
                        clearActiveModules();
                        setCampaignId(campaign);
                        NavigatorConfig.getInstance().reload();
                        LocalBroadcastManager
                                .getInstance(ctx)
                                .sendBroadcast(intent);
                    }
                }
            }.execute(token);
        });
    }

    public static void setCampaignId(String campaignId) {
        Log.i(TAG, "campaign id set to '" + campaignId + "'");
        getSharedPrefs(App.getApp()).edit().putString(CIMS_CAMPAIGN_ID, campaignId).apply();
    }

    public static String getCampaignId() {
        return getSharedPrefs(App.getApp()).getString(CIMS_CAMPAIGN_ID, null);
    }
}
