package org.cimsbioko.campaign;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.cimsbioko.R;
import org.cimsbioko.utilities.HttpUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.cimsbioko.utilities.AccountUtils.getToken;
import static org.cimsbioko.utilities.CampaignUtils.*;
import static org.cimsbioko.utilities.ConfigUtils.getPreferenceBool;
import static org.cimsbioko.utilities.HttpUtils.encodeBearerCreds;
import static org.cimsbioko.utilities.IOUtils.store;
import static org.cimsbioko.utilities.IOUtils.streamToFile;
import static org.cimsbioko.utilities.NetUtils.isConnected;
import static org.cimsbioko.utilities.NetUtils.isWiFiConnected;
import static org.cimsbioko.utilities.SetupUtils.setCampaignId;

public class CampaignUpdateService extends JobIntentService {

    private static final String TAG = CampaignUpdateService.class.getSimpleName();
    public static final String CAMPAIGN_UPDATE_AVAILABLE = "CAMPAIGN_UPDATE_AVAILABLE";
    public static final String CIMS_CAMPAIGN_ID = "cims-campaign-id";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        Context ctx = getApplicationContext();

        boolean wifiOnlyEnabled = getPreferenceBool(ctx, ctx.getString(R.string.wifi_sync_key), true);
        if (wifiOnlyEnabled && !isWiFiConnected()) {
            Log.i(TAG, "ignoring update request, not on wi-fi");
            return;
        }

        if (isConnected() && downloadedCampaignExists()) {
            try {
                String token = getToken(), hash = getCampaignHash();
                URL url = new URL(getCampaignUrl());
                HttpURLConnection c = HttpUtils.get(url, null, encodeBearerCreds(token), hash);
                int response = c.getResponseCode();
                switch (response) {
                    case HTTP_OK:
                        Log.i(TAG, "campaign update available");
                        try {
                            File newFile = getCampaignTempFile(), newFingerprintFile = getCampaignTempFingerprintFile();
                            streamToFile(c.getInputStream(), newFile);
                            String etag = c.getHeaderField("ETag"), campaignId = c.getHeaderField(CIMS_CAMPAIGN_ID);
                            if (etag != null) {
                                store(newFingerprintFile, etag);
                            }
                            setCampaignId(campaignId);
                            LocalBroadcastManager
                                    .getInstance(getApplicationContext())
                                    .sendBroadcast(new Intent(CAMPAIGN_UPDATE_AVAILABLE));
                        } catch (InterruptedException e) {
                            Log.w(TAG, "campaign download interrupted");
                        }
                        break;
                    case HTTP_NOT_MODIFIED:
                        Log.i(TAG, "campaign is latest available");
                        break;
                    default:
                        Log.e(TAG, "unexpected http response: " + response);
                }
            } catch (AuthenticatorException | OperationCanceledException | IOException e) {
                Log.d(TAG, "aborting campaign update request, failed to get token: " + e.getMessage());
            }
        }
    }
}
