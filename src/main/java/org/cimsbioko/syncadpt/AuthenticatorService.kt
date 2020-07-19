package org.cimsbioko.syncadpt

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthenticatorService : Service() {

    private lateinit var auth: AccountAuthenticator

    override fun onCreate() {
        auth = AccountAuthenticator(this)
    }

    override fun onBind(intent: Intent): IBinder {
        return auth.iBinder
    }
}