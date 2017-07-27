package org.openhds.mobile.offlinedb;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.openhds.mobile.utilities.SyncUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class OfflineDbService extends IntentService {

    private static final String TAG = OfflineDbService.class.getSimpleName();

    public OfflineDbService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            SyncUtils.makeOfflineDbAvailable(getApplicationContext());
        } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
            Log.e(TAG, "failed to make offline db available for installation", e);
        }
    }
}
