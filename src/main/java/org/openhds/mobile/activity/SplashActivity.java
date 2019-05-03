package org.openhds.mobile.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import org.openhds.mobile.R;
import org.openhds.mobile.provider.FormsProviderAPI;
import org.openhds.mobile.utilities.NotificationUtils;

import java.util.List;

/**
 * This activity simply forwards to the actual opening activity for the application, showing an image by its style
 * instead of inflating a layout or setting any view. This ensures the image is available immediately, and the
 * actual opening activity is available as soon as it is ready.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isODKInstalled()) {
            promptODKInstall();
        } else if (needsPermissions()) {
            askForPermissions();
        } else {
            startApp();
        }
    }

    private boolean needsPermissions() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    private void askForPermissions() {
        if (needsPermissions()) {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST: {
                boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (granted) {
                    startApp();
                }
            }
        }
    }

    private void promptODKInstall() {

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    launchODKMarketInstall();
                }
                finish();
            }

            private void launchODKMarketInstall() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=org.odk.collect.android"));
                startActivity(intent);
            }
        };

        DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.odk_required)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.odk_install_prompt)
                .setNegativeButton(R.string.quit_label, clickListener)
                .setPositiveButton(R.string.install_label, clickListener)
                .setOnCancelListener(cancelListener)
                .show();
    }

    private void startApp() {
        NotificationUtils.createChannels(getApplicationContext());
        launchLogin();
    }

    private void launchLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isODKInstalled() {
        Intent odkFormsIntent = new Intent(Intent.ACTION_EDIT, FormsProviderAPI.FormsColumns.CONTENT_URI);
        List<ResolveInfo> intentMatches = getPackageManager().queryIntentActivities(odkFormsIntent, 0);
        return !intentMatches.isEmpty();
    }
}