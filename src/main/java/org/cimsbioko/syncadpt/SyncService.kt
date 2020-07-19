package org.cimsbioko.syncadpt

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SyncService : Service() {
    override fun onCreate() {
        super.onCreate()
        synchronized(CREATE_LOCK) {
            if (adapter == null) {
                adapter = SyncAdapter(applicationContext, true)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return adapter!!.syncAdapterBinder
    }

    companion object {
        private val CREATE_LOCK = Any()
        private var adapter: SyncAdapter? = null
    }
}