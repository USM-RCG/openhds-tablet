package org.cimsbioko.offlinedb

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import org.cimsbioko.utilities.SyncUtils.makeOfflineDbAvailable
import java.io.IOException
import java.security.NoSuchAlgorithmException

class OfflineDbService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        try {
            makeOfflineDbAvailable(applicationContext)
        } catch (e: IOException) {
            Log.e(TAG, "failed to make offline db available for installation", e)
        } catch (e: InterruptedException) {
            Log.e(TAG, "failed to make offline db available for installation", e)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "failed to make offline db available for installation", e)
        }
    }

    companion object {
        private val TAG = OfflineDbService::class.java.simpleName
        private const val JOB_ID = 0xFC
        @JvmStatic
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, OfflineDbService::class.java, JOB_ID, intent)
        }
    }
}