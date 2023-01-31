package org.cimsbioko.utilities

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cimsbioko.App
import org.cimsbioko.R
import org.cimsbioko.campaign.CampaignUpdateService
import org.cimsbioko.navconfig.NavigatorConfig
import org.cimsbioko.provider.FormsProviderAPI
import org.cimsbioko.syncadpt.Constants
import org.cimsbioko.utilities.AccountUtils.firstAccount
import org.cimsbioko.utilities.CampaignDownloadResult.Success
import org.cimsbioko.utilities.CampaignUtils.downloadedCampaignExists
import org.cimsbioko.utilities.ConfigUtils.clearActiveModules
import org.cimsbioko.utilities.ConfigUtils.getSharedPrefs
import org.cimsbioko.utilities.FileUtils.getFingerprintFile
import org.cimsbioko.utilities.FormsHelper.hasFormsWithIds
import org.cimsbioko.utilities.IOUtils.store
import org.cimsbioko.utilities.LoginUtils.launchLogin
import org.cimsbioko.utilities.MessageUtils.showLongToast
import org.cimsbioko.utilities.NotificationUtils.createChannels
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SetupUtils {

    private val TAG = SetupUtils::class.java.simpleName
    const val CAMPAIGN_DOWNLOADED_ACTION = "CAMPAIGN_DOWNLOADED"
    const val CAMPAIGN_FILENAME = "campaign.zip"

    fun minimumRequirementsMet(ctx: Context): Boolean = isFormsAppInstalled(ctx.packageManager)

    fun setupRequirementsMet(ctx: Context): Boolean = minimumRequirementsMet(ctx)
            && isAccountInstalled
            && isConfigAvailable
            && isDataAvailable(ctx)
            && hasCampaignForms()

    fun startApp(source: Activity) = launchLogin(source)

    fun createNotificationChannels(ctx: Context) = createChannels(ctx.applicationContext)

    val isConfigAvailable: Boolean
        get() = downloadedCampaignExists()

    fun isDataAvailable(ctx: Context): Boolean = SyncUtils.downloadedContentBefore(ctx)

    val isAccountInstalled: Boolean
        get() = firstAccount != null

    fun isFormsAppInstalled(manager: PackageManager): Boolean {
        val formsIntent = Intent(Intent.ACTION_EDIT, FormsProviderAPI.FormsColumns.CONTENT_URI)
        val intentMatches = manager.queryIntentActivities(formsIntent, 0)
        return intentMatches.isNotEmpty()
    }

    fun promptFormsAppInstall(activity: Activity) {
        val marketIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=org.cimsbioko.forms")
        }
        if (marketIntent.resolveActivity(activity.packageManager) != null) {
            val clickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, which ->
                try {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        activity.startActivity(marketIntent)
                    }
                    activity.finish()
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, "failed to launch play app by market uri", e)
                }
            }
            val cancelListener = DialogInterface.OnCancelListener { activity.finish() }
            AlertDialog.Builder(activity)
                    .setTitle(R.string.forms_app_required)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.forms_app_install_prompt)
                    .setNegativeButton(R.string.quit_label, clickListener)
                    .setPositiveButton(R.string.install_label, clickListener)
                    .setOnCancelListener(cancelListener)
                    .show()
        } else {
            val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=org.cimsbioko.forms")
            )
            try {
                activity.startActivity(webIntent)
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "failed to launch play store via web uri", e)
            }
        }
    }

    suspend fun getToken(activity: Activity): String? = suspendCoroutine { cont ->
        runCatching {
            AccountManager
                    .get(activity.applicationContext)
                    .getAuthTokenByFeatures(Constants.ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE_DEVICE, null, activity, null, null,
                            { future ->
                                try {
                                    val result = future.result
                                    cont.resume(result?.getString(AccountManager.KEY_AUTHTOKEN))
                                } catch (e: Exception) {
                                    cont.resumeWithException(e)
                                }
                            }, null)
        }.getOrElse { t -> cont.resumeWithException(t) }
    }

    private suspend fun downloadCampaign(token: String): CampaignDownloadResult {
        val campaigns = withContext(Dispatchers.IO) { runCatching { CampaignUtils.getCampaigns(token) } }
                .getOrElse { return@downloadCampaign CampaignDownloadResult.Failure("fetching assigned campaigns failed: $it") }
        return if (campaigns.length() > 0) {
            val firstCampaign = campaigns.getJSONObject(0)
            val campaignId = firstCampaign.getString("uuid")
            val campaignName = firstCampaign.getString("name")
            Log.i("CampaignTask", "downloading campaign '$campaignName', uuid: $campaignId")
            withContext(Dispatchers.IO) { runCatching { CampaignUtils.downloadCampaignFile(token, campaignId, CampaignUtils.downloadedCampaignFile) } }
                    .getOrElse { CampaignDownloadResult.Failure("Download failed: $it") }
        } else CampaignDownloadResult.Failure("No campaign assigned to this device")
    }

    suspend fun downloadConfig(activity: Activity) {
        val token = runCatching { getToken(activity) }.getOrNull()
        if (token != null) {
            val ctx = activity.applicationContext
            val campaignDownloadResult = downloadCampaign(token)
            if (campaignDownloadResult is CampaignDownloadResult.Failure) {
                showLongToast(ctx, campaignDownloadResult.error)
            } else {
                val result = campaignDownloadResult as Success
                val etag = result.etag
                val campaign = result.campaign
                store(getFingerprintFile(result.downloadedFile), etag)
                val intent = Intent(CAMPAIGN_DOWNLOADED_ACTION)
                clearActiveModules()
                campaignId = campaign
                NavigatorConfig.instance.reload()
                LocalBroadcastManager
                        .getInstance(ctx)
                        .sendBroadcast(intent)
            }
        }
    }

    var campaignId: String?
        get() = getSharedPrefs(App.instance).getString(CampaignUpdateService.CIMS_CAMPAIGN_ID, null)
        set(campaignId) {
            Log.i(TAG, "campaign id set to '$campaignId'")
            getSharedPrefs(App.instance).edit { putString(CampaignUpdateService.CIMS_CAMPAIGN_ID, campaignId) }
        }

    fun hasCampaignForms(): Boolean = hasFormsWithIds(NavigatorConfig.instance.formIds)

    fun downloadForms(ctx: Context) {
        val i = Intent("org.cimsbioko.forms.FORM_DOWNLOAD")
        i.type = "vnd.android.cursor.dir/vnd.cimsforms.form"
        ctx.startActivity(i)
    }
}