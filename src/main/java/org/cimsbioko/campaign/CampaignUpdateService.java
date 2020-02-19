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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import static org.cimsbioko.utilities.SetupUtils.getCampaignId;

public class CampaignUpdateService extends JobIntentService {

    private static final String TAG = CampaignUpdateService.class.getSimpleName();
    public static final String CAMPAIGN_UPDATE_AVAILABLE = "CAMPAIGN_UPDATE_AVAILABLE";
    public static final String CIMS_CAMPAIGN_ID = "cims-campaign-id";

    private static final int JOB_ID = 0xFA;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, CampaignUpdateService.class, JOB_ID, intent);
    }

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
                String token = getToken(), hash = getCampaignHash(), existingCampaignId = getCampaignId();
                JSONArray campaigns = getCampaigns(token);
                if (campaigns.length() > 0) {
                    JSONObject firstCampaign = campaigns.getJSONObject(0);
                    String newCampaignId = firstCampaign.getString("uuid"), newCampaignName = firstCampaign.getString("name");
                    boolean campaignChanged = !existingCampaignId.equals(newCampaignId);
                    if (campaignChanged) {
                        Log.i(TAG, "campaign changed, old = " + existingCampaignId + ", new = " +
                                newCampaignId + " (" + newCampaignName + ")");
                    }
                    URL campaignUrl = new URL(getCampaignUrl(newCampaignId));
                    String existingEtag = campaignChanged ? null : hash;
                    HttpURLConnection c = HttpUtils.get(campaignUrl, null, encodeBearerCreds(token), existingEtag);
                    int response = c.getResponseCode();
                    switch (response) {
                        case HTTP_OK:
                            Log.i(TAG, "campaign update available");
                            try {
                                File newFile = getCampaignTempFile(), newFingerprintFile = getCampaignTempFingerprintFile();
                                streamToFile(c.getInputStream(), newFile);
                                String newEtag = c.getHeaderField("ETag"), campaignId = c.getHeaderField(CIMS_CAMPAIGN_ID);
                                if (newEtag != null) {
                                    store(newFingerprintFile, newEtag);
                                }
                                Intent updateMsg = new Intent(CAMPAIGN_UPDATE_AVAILABLE);
                                updateMsg.putExtra("campaignChanged", campaignChanged);
                                updateMsg.putExtra("campaignId", campaignId);
                                LocalBroadcastManager
                                        .getInstance(getApplicationContext())
                                        .sendBroadcast(updateMsg);
                            } catch (InterruptedException e) {
                                Log.w(TAG, "campaign download interrupted");
                            }
                            break;
                        case HTTP_NOT_MODIFIED:
                            Log.i(TAG, "campaign is up-to-date");
                            break;
                        default:
                            Log.e(TAG, "unexpected http response: " + response);
                    }
                } else {
                    Log.i(TAG, "no campaign assigned to device");
                }
            } catch (AuthenticatorException | OperationCanceledException | IOException | JSONException e) {
                Log.d(TAG, "aborting campaign update request, failed to get token: " + e.getMessage());
            }
        }
    }
}
