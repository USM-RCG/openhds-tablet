package org.cimsbioko.campaign

import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.cimsbioko.R
import org.cimsbioko.utilities.*
import org.cimsbioko.utilities.AccountUtils.token
import org.cimsbioko.utilities.CampaignUtils.campaignHash
import org.cimsbioko.utilities.CampaignUtils.campaignTempFile
import org.cimsbioko.utilities.CampaignUtils.campaignTempFingerprintFile
import org.cimsbioko.utilities.CampaignUtils.downloadedCampaignExists
import org.cimsbioko.utilities.CampaignUtils.getCampaignUrl
import org.cimsbioko.utilities.CampaignUtils.getCampaigns
import org.json.JSONException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CampaignUpdateService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val ctx = applicationContext
        val wifiOnlyEnabled = ConfigUtils.getPreferenceBool(ctx, ctx.getString(R.string.wifi_sync_key), true)
        if (wifiOnlyEnabled && !NetUtils.isWiFiConnected) {
            Log.i(TAG, "ignoring update request, not on wi-fi")
            return
        }
        if (NetUtils.isConnected && downloadedCampaignExists()) {
            try {
                val token = token
                val hash = campaignHash
                val existingCampaignId = SetupUtils.getCampaignId()
                val campaigns = getCampaigns(token)
                if (campaigns.length() > 0) {
                    val firstCampaign = campaigns.getJSONObject(0)
                    val newCampaignId = firstCampaign.getString("uuid")
                    val newCampaignName = firstCampaign.getString("name")
                    val campaignChanged = existingCampaignId != newCampaignId
                    if (campaignChanged) {
                        Log.i(TAG, "campaign changed, old = " + existingCampaignId + ", new = " +
                                newCampaignId + " (" + newCampaignName + ")")
                    }
                    val campaignUrl = URL(getCampaignUrl(newCampaignId))
                    val existingEtag = if (campaignChanged) null else hash
                    val c = HttpUtils.get(campaignUrl, null, HttpUtils.encodeBearerCreds(token), existingEtag)
                    when (val response = c.responseCode) {
                        HttpURLConnection.HTTP_OK -> {
                            Log.i(TAG, "campaign update available")
                            try {
                                val newFile = campaignTempFile
                                val newFingerprintFile = campaignTempFingerprintFile
                                IOUtils.streamToFile(c.inputStream, newFile)
                                val newEtag = c.getHeaderField("ETag")
                                val campaignId = c.getHeaderField(CIMS_CAMPAIGN_ID)
                                if (newEtag != null) {
                                    IOUtils.store(newFingerprintFile, newEtag)
                                }
                                val updateMsg = Intent(CAMPAIGN_UPDATE_AVAILABLE)
                                updateMsg.putExtra("campaignChanged", campaignChanged)
                                updateMsg.putExtra("campaignId", campaignId)
                                LocalBroadcastManager
                                        .getInstance(applicationContext)
                                        .sendBroadcast(updateMsg)
                            } catch (e: InterruptedException) {
                                Log.w(TAG, "campaign download interrupted")
                            }
                        }
                        HttpURLConnection.HTTP_NOT_MODIFIED -> Log.i(TAG, "campaign is up-to-date")
                        else -> Log.e(TAG, "unexpected http response: $response")
                    }
                } else {
                    Log.i(TAG, "no campaign assigned to device")
                }
            } catch (e: AuthenticatorException) {
                Log.d(TAG, "aborting campaign update request, failed to get token: " + e.message)
            } catch (e: OperationCanceledException) {
                Log.d(TAG, "aborting campaign update request, failed to get token: " + e.message)
            } catch (e: IOException) {
                Log.d(TAG, "aborting campaign update request, failed to get token: " + e.message)
            } catch (e: JSONException) {
                Log.d(TAG, "aborting campaign update request, failed to get token: " + e.message)
            }
        }
    }

    companion object {

        private val TAG = CampaignUpdateService::class.java.simpleName
        const val CAMPAIGN_UPDATE_AVAILABLE = "CAMPAIGN_UPDATE_AVAILABLE"
        const val CIMS_CAMPAIGN_ID = "cims-campaign-id"
        private const val JOB_ID = 0xFA

        @JvmStatic
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, CampaignUpdateService::class.java, JOB_ID, intent)
        }
    }
}