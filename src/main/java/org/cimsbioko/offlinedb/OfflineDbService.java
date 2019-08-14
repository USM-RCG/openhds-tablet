package org.cimsbioko.offlinedb;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.cimsbioko.utilities.SyncUtils.makeOfflineDbAvailable;

public class OfflineDbService extends IntentService {

    private static final String TAG = OfflineDbService.class.getSimpleName();

    public OfflineDbService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            makeOfflineDbAvailable(getApplicationContext());
        } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
            Log.e(TAG, "failed to make offline db available for installation", e);
        }
    }
}
