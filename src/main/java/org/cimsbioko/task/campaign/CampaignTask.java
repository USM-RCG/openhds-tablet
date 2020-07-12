package org.cimsbioko.task.campaign;

import android.os.AsyncTask;
import android.util.Log;
import org.cimsbioko.utilities.CampaignDownloadResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Objects;

import static org.cimsbioko.utilities.CampaignUtils.*;

public class CampaignTask extends AsyncTask<String, Void, CampaignDownloadResult> {

    @Override
    protected CampaignDownloadResult doInBackground(String... tokens) {
        Objects.requireNonNull(tokens);
        if (tokens.length > 0) {
            String token = tokens[0];
            try {
                JSONArray campaigns = getCampaigns(token);
                if (campaigns.length() > 0) {
                    JSONObject firstCampaign = campaigns.getJSONObject(0);
                    String campaignId = firstCampaign.getString("uuid"), campaignName = firstCampaign.getString("name");
                    Log.i(CampaignTask.class.getSimpleName(), "downloading campaign '" + campaignName + "', uuid: " + campaignId);
                    return downloadCampaignFile(token, campaignId,  getDownloadedCampaignFile());
                } else {
                    return new CampaignDownloadResult.Failure("No campaign assigned to this device");
                }
            } catch (IOException | JSONException e) {
                return new CampaignDownloadResult.Failure("Download failed: " + e);
            }
        } else {
            return new CampaignDownloadResult.Failure("Download failed: not authenticated");
        }
    }
}
