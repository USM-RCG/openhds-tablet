package org.cimsbioko.utilities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import org.cimsbioko.R

object NotificationUtils {

    const val PROGRESS_NOTIFICATION_RATE_MILLIS: Long = 1000
    const val SYNC_CHANNEL_ID = "Sync"
    const val TAG = "NotificationUtils"

    val notificationIcon: Int
        get() = R.drawable.ic_silhouette

    fun getNotificationColor(ctx: Context?): Int {
        return ContextCompat.getColor(ctx!!, R.color.Blue)
    }

    fun getNotificationManager(ctx: Context): NotificationManager {
        return ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun createChannels(ctx: Context) {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val manager = getNotificationManager(ctx)
            val syncChannel = NotificationChannel(SYNC_CHANNEL_ID,
                    ctx.getString(R.string.sync_notifications_channel_name),
                    NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(syncChannel)
        }
    }
}

/**
 * Requests notification permissions from users when they are running Android 13+ and they have not already granted them.
 */
fun ComponentActivity.requestNotificationPermissions() {
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            val launcher = this.registerForActivityResult(RequestPermission()) { isGranted ->
                Log.d(NotificationUtils.TAG, "User granted notification permissions? $isGranted")
            }
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}