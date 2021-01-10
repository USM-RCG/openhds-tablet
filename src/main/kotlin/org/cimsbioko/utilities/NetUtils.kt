package org.cimsbioko.utilities

import android.content.Context
import android.net.ConnectivityManager
import org.cimsbioko.App

object NetUtils {

    private val connMan: ConnectivityManager
        get() = App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isConnected: Boolean
        get() {
            val cm = connMan
            val activeNetwork = cm.activeNetworkInfo ?: return false
            return activeNetwork.isConnected
        }

    val isWiFiConnected: Boolean
        get() {
            val cm = connMan
            val activeNetwork = cm.activeNetworkInfo ?: return false
            return activeNetwork.isConnected && activeNetwork.type == ConnectivityManager.TYPE_WIFI
        }

}