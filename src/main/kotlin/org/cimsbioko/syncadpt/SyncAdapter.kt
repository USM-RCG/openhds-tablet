package org.cimsbioko.syncadpt

import android.accounts.Account
import android.accounts.AccountManager
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.net.nsd.NsdManager
import android.os.Bundle
import android.util.Log
import org.cimsbioko.R
import org.cimsbioko.sidecar.Sidecar
import org.cimsbioko.utilities.ConfigUtils.getPreferenceBool
import org.cimsbioko.utilities.NetUtils.isWiFiConnected
import org.cimsbioko.utilities.SyncUtils.downloadUpdate
import org.cimsbioko.utilities.SyncUtils.getLocalSyncEndpoint
import org.cimsbioko.utilities.SyncUtils.getRemoteSyncEndpoint
import java.net.MalformedURLException

class SyncAdapter(context: Context, autoInitialize: Boolean) : AbstractThreadedSyncAdapter(context, autoInitialize) {

    override fun onPerformSync(account: Account, extras: Bundle, authority: String, provider: ContentProviderClient, syncResult: SyncResult) {
        val ctx = context
        val autoSyncEnabled = getPreferenceBool(ctx, ctx.getString(R.string.use_auto_sync_key), true)
        val manualSyncRequested = extras.getBoolean(ctx.getString(R.string.manual_sync_key), false)
        val sidecarEnabled = getPreferenceBool(ctx, ctx.getString(R.string.use_sidecar_key), false)
        val wifiOnlyEnabled = getPreferenceBool(ctx, ctx.getString(R.string.wifi_sync_key), true)
        if (wifiOnlyEnabled && !isWiFiConnected) {
            Log.w(TAG, "user settings require wi-fi, not syncing")
        } else if (autoSyncEnabled || manualSyncRequested) {
            try {
                if (wifiOnlyEnabled && sidecarEnabled) {
                    val info = Sidecar.discover(ctx.getSystemService(Context.NSD_SERVICE) as NsdManager, 30)
                    val endpoint = getLocalSyncEndpoint(ctx, info)
                    Log.i(TAG, "local sync $endpoint")
                    downloadUpdate(ctx, endpoint, syncResult = syncResult)
                } else {
                    val endpoint = getRemoteSyncEndpoint(ctx)
                    Log.i(TAG, "remote sync $endpoint")
                    val token = AccountManager.get(ctx).blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE_DEVICE, true)
                    downloadUpdate(ctx, endpoint, token, syncResult)
                }
            } catch (e: MalformedURLException) {
                Log.w(TAG, "bad endpoint url", e)
                syncResult.stats.numParseExceptions += 1
            } catch (e: Exception) {
                Log.w(TAG, e.message)
                syncResult.stats.numParseExceptions += 1
            }
        } else {
            Log.d(TAG, "sync adapter ran, but ignored - auto-sync disabled by user")
        }
    }

    override fun onSyncCanceled() {
        super.onSyncCanceled()
        Log.i(TAG, "sync cancelled")
    }

    companion object {
        private val TAG = SyncAdapter::class.java.simpleName
    }
}