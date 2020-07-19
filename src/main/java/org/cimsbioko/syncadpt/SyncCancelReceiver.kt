package org.cimsbioko.syncadpt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.cimsbioko.utilities.SyncUtils.cancelUpdate

class SyncCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        cancelUpdate(context)
    }
}