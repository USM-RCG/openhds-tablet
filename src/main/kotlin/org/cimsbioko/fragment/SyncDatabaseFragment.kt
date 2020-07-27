package org.cimsbioko.fragment

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.cimsbioko.App
import org.cimsbioko.App.Companion.getApp
import org.cimsbioko.R
import org.cimsbioko.offlinedb.OfflineDbService
import org.cimsbioko.offlinedb.OfflineDbService.Companion.enqueueWork
import org.cimsbioko.search.IndexingService.Companion.queueFullReindex
import org.cimsbioko.search.Utils.isAutoReindexingEnabled
import org.cimsbioko.utilities.FileUtils.getDatabaseFile
import org.cimsbioko.utilities.FileUtils.getFingerprintFile
import org.cimsbioko.utilities.MessageUtils.showLongToast
import org.cimsbioko.utilities.NotificationUtils.getNotificationManager
import org.cimsbioko.utilities.SyncUtils.DatabaseInstallationListener
import org.cimsbioko.utilities.SyncUtils.SYNC_NOTIFICATION_ID
import org.cimsbioko.utilities.SyncUtils.canUpdateDatabase
import org.cimsbioko.utilities.SyncUtils.checkForUpdate
import org.cimsbioko.utilities.SyncUtils.getDatabaseFingerprint
import org.cimsbioko.utilities.SyncUtils.installUpdate
import java.io.File

/**
 * Allow user to check for db updates, download and apply them.
 */
class SyncDatabaseFragment : Fragment(), View.OnClickListener, DatabaseInstallationListener {

    private lateinit var lastUpdated: TextView
    private lateinit var fingerprint: TextView
    private lateinit var checkButton: Button
    private lateinit var updateButton: Button
    private lateinit var observer: ContentObserver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.sync_database_fragment, container, false).apply {
            lastUpdated = findViewById(R.id.sync_updated_column)
            fingerprint = findViewById(R.id.sync_fingerprint_column)
            checkButton = findViewById(R.id.sync_check_button)
            checkButton.setOnClickListener(this@SyncDatabaseFragment)
            updateButton = findViewById(R.id.sync_update_button)
            updateButton.setOnClickListener(this@SyncDatabaseFragment)
            updateStatus()
        }
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        observer = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                updateStatus()
            }
        }.also { ctx.contentResolver.registerContentObserver(App.CONTENT_BASE_URI, false, it) }
        enqueueWork(ctx.applicationContext, Intent(ctx, OfflineDbService::class.java))
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    override fun onDestroy() {
        activity?.contentResolver?.unregisterContentObserver(observer)
        super.onDestroy()
    }

    private val fingerprintFile: File
        get() = getFingerprintFile(getDatabaseFile(getApp()))

    private fun getLastUpdated(): CharSequence = activity?.let { ctx ->
        fingerprintFile.let { f ->
            if (f.exists()) DateUtils.getRelativeTimeSpanString(ctx, f.lastModified(), false)
            else ctx.getString(R.string.sync_database_updated_never)
        }
    } ?: ""

    private fun updateStatus() {
        activity?.let { ctx ->
            val fpVal = getDatabaseFingerprint(ctx)
            fingerprint.text = if (fpVal.length > 8) fpVal.substring(0, 8) + '\u2026' else fpVal
            lastUpdated.text = getLastUpdated()
            updateButton.isEnabled = canUpdateDatabase(ctx)
        }
    }

    override fun onClick(v: View) {
        if (v === checkButton) checkForUpdate() else if (v === updateButton) activity?.let { installUpdate(it, this) }
    }

    override fun installed() {
        activity?.let { ctx ->
            updateStatus()
            getNotificationManager(ctx).apply { cancel(SYNC_NOTIFICATION_ID) }
            showLongToast(ctx, ctx.getString(R.string.sync_database_updated))
            if (isAutoReindexingEnabled(ctx)) queueFullReindex(ctx)
        }
    }
}