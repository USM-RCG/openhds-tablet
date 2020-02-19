package org.cimsbioko.offlinedb;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.cimsbioko.utilities.SyncUtils.makeOfflineDbAvailable;

public class OfflineDbService extends JobIntentService {

    private static final String TAG = OfflineDbService.class.getSimpleName();

    private static final int JOB_ID = 0xFC;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, OfflineDbService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        try {
            makeOfflineDbAvailable(getApplicationContext());
        } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
            Log.e(TAG, "failed to make offline db available for installation", e);
        }
    }
}
