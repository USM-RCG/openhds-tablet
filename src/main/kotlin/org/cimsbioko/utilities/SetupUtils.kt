package org.cimsbioko.utilities

import android.Manifest.permission
import android.accounts.*
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.cimsbioko.App
import org.cimsbioko.R
import org.cimsbioko.campaign.CampaignUpdateService
import org.cimsbioko.navconfig.NavigatorConfig
import org.cimsbioko.provider.FormsProviderAPI
import org.cimsbioko.syncadpt.Constants
import org.cimsbioko.task.CampaignTask
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
import java.io.IOException

object SetupUtils {

    private val TAG = SetupUtils::class.java.simpleName
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= 19) emptyArray() /**/ else arrayOf(permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE)
    const val CAMPAIGN_DOWNLOADED_ACTION = "CAMPAIGN_DOWNLOADED"
    const val CAMPAIGN_FILENAME = "campaign.zip"

    fun setupRequirementsMet(ctx: Context): Boolean {
        return (hasRequiredPermissions(ctx)
                && isFormsAppInstalled(ctx.packageManager)
                && isAccountInstalled
                && isConfigAvailable
                && isDataAvailable(ctx)
                && hasCampaignForms())
    }

    fun hasRequiredPermissions(ctx: Context?): Boolean {
        for (perm in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(ctx!!, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun startApp(source: Activity) = launchLogin(source)

    fun createNotificationChannels(ctx: Context) = createChannels(ctx.applicationContext)

    fun askForPermissions(activity: Activity, requestCode: Int) {
        if (needsPermissions(activity)) {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, requestCode)
        }
    }

    private fun needsPermissions(ctx: Context): Boolean = !hasRequiredPermissions(ctx)

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
        val clickListener: DialogInterface.OnClickListener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    launchFormsAppMarketInstall()
                }
                activity.finish()
            }

            private fun launchFormsAppMarketInstall() {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("market://details?id=org.cimsbioko.forms")
                activity.startActivity(intent)
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
    }

    fun getToken(activity: Activity, callback: AccountManagerCallback<Bundle>?) {
        AccountManager
                .get(activity.applicationContext)
                .getAuthTokenByFeatures(Constants.ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE_DEVICE, null, activity, null, null, callback, null)
    }

    fun downloadConfig(activity: Activity) {
        getToken(activity, AccountManagerCallback { future: AccountManagerFuture<Bundle> ->

            val ctx = activity.applicationContext

            // Extract the auth token for the active account
            val token = try {
                future.result.getString(AccountManager.KEY_AUTHTOKEN)
            } catch (e: AuthenticatorException) {
                showLongToast(activity, "Failed to get auth token: " + e.message)
                null
            } catch (e: IOException) {
                showLongToast(activity, "Failed to get auth token: " + e.message)
                null
            } catch (e: OperationCanceledException) {
                showLongToast(activity, "Failed to get auth token: " + e.message)
                null
            }

            // Download the campaign file and send a local broadcast message when it finishes
            token?.run {
                object : CampaignTask() {
                    override fun onPostExecute(campaignDownloadResult: CampaignDownloadResult) {
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
                }.execute(this@run)
            }
        })
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