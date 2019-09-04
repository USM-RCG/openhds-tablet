package org.cimsbioko.utilities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import org.cimsbioko.R;
import org.cimsbioko.provider.FormsProviderAPI;

import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static org.cimsbioko.syncadpt.Constants.ACCOUNT_TYPE;
import static org.cimsbioko.syncadpt.Constants.AUTHTOKEN_TYPE_DEVICE;
import static org.cimsbioko.utilities.LoginUtils.launchLogin;
import static org.cimsbioko.utilities.SyncUtils.downloadedContentBefore;

public class SetupUtils {

    private static final String TAG = SetupUtils.class.getSimpleName();
    private static final String[] REQUIRED_PERMISSIONS = new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};

    public static boolean setupRequirementsMet(Context ctx) {
        return hasRequiredPermissions(ctx)
                && isFormsAppInstalled(ctx.getPackageManager())
                && isAccountInstalled(ctx)
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

    public static void getToken(final Activity activity) {
        AccountManager
                .get(activity.getBaseContext())
                .getAuthTokenByFeatures(ACCOUNT_TYPE, AUTHTOKEN_TYPE_DEVICE, null, activity, null, null,
                        future -> {
                            try {
                                Bundle result = future.getResult();
                                result.getString(AccountManager.KEY_AUTHTOKEN);
                            } catch (Exception e) {
                                Log.e(TAG, "failed to retrieve token", e);
                            }
                        }, null);
    }

}
