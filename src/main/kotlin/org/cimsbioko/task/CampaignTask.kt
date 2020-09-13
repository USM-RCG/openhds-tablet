package org.cimsbioko.task

import android.os.AsyncTask
import android.util.Log
import org.cimsbioko.utilities.CampaignDownloadResult
import org.cimsbioko.utilities.CampaignUtils.downloadCampaignFile
import org.cimsbioko.utilities.CampaignUtils.downloadedCampaignFile
import org.cimsbioko.utilities.CampaignUtils.getCampaigns

open class CampaignTask : AsyncTask<String, Void, CampaignDownloadResult>() {
    override fun doInBackground(vararg tokens: String): CampaignDownloadResult {
        return tokens.firstOrNull()?.let { token ->
            try {
                val campaigns = getCampaigns(token)
                if (campaigns.length() > 0) {
                    val firstCampaign = campaigns.getJSONObject(0)
                    val campaignId = firstCampaign.getString("uuid")
                    val campaignName = firstCampaign.getString("name")
                    Log.i(CampaignTask::class.java.simpleName, "downloading campaign '$campaignName', uuid: $campaignId")
                    downloadCampaignFile(token, campaignId, downloadedCampaignFile)
                } else {
                    CampaignDownloadResult.Failure("No campaign assigned to this device")
                }
            } catch (e: Exception) {
                CampaignDownloadResult.Failure("Download failed: $e")
            }
        } ?: CampaignDownloadResult.Failure("Download failed: not authenticated")
    }
}