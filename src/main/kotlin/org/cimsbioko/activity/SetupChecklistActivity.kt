package org.cimsbioko.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.navigation.NavigationView
import org.cimsbioko.R
import org.cimsbioko.utilities.MessageUtils.showShortToast
import org.cimsbioko.utilities.NetUtils.isConnected
import org.cimsbioko.utilities.SetupUtils.CAMPAIGN_DOWNLOADED_ACTION
import org.cimsbioko.utilities.SetupUtils.askForPermissions
import org.cimsbioko.utilities.SetupUtils.downloadConfig
import org.cimsbioko.utilities.SetupUtils.downloadForms
import org.cimsbioko.utilities.SetupUtils.getToken
import org.cimsbioko.utilities.SetupUtils.hasCampaignForms
import org.cimsbioko.utilities.SetupUtils.hasRequiredPermissions
import org.cimsbioko.utilities.SetupUtils.isAccountInstalled
import org.cimsbioko.utilities.SetupUtils.isConfigAvailable
import org.cimsbioko.utilities.SetupUtils.isDataAvailable
import org.cimsbioko.utilities.SetupUtils.isFormsAppInstalled
import org.cimsbioko.utilities.SetupUtils.promptFormsAppInstall
import org.cimsbioko.utilities.SetupUtils.startApp
import org.cimsbioko.utilities.SyncUtils.DATA_INSTALLED_ACTION
import org.cimsbioko.utilities.SyncUtils.checkForUpdate

class SetupChecklistActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var permissionsCheckbox: CheckBox
    private lateinit var appsCheckbox: CheckBox
    private lateinit var connectCheckbox: CheckBox
    private lateinit var configCheckbox: CheckBox
    private lateinit var dataCheckbox: CheckBox
    private lateinit var formsCheckbox: CheckBox
    private lateinit var setupButton: Button
    private lateinit var broadcastReceiver: BroadcastReceiver

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.cims_setup)
        setContentView(R.layout.setup_checklist)

        val toolbar = findViewById<Toolbar>(R.id.setup_toolbar).also { setSupportActionBar(it) }
        findViewById<DrawerLayout>(R.id.setup_drawer_layout).also {
            ActionBarDrawerToggle(this, it, toolbar, 0, 0).apply {
                it.addDrawerListener(this)
                syncState()
            }
        }
        findViewById<NavigationView>(R.id.setup_navigation_view).also {
            it.setNavigationItemSelectedListener(this)
        }

        permissionsCheckbox = findViewById(R.id.grantPermsCheckbox)
        appsCheckbox = findViewById(R.id.installAppsCheckbox)
        connectCheckbox = findViewById(R.id.serverConnectCheckbox)
        configCheckbox = findViewById(R.id.configDownloadCheckbox)
        dataCheckbox = findViewById(R.id.dataDownloadCheckbox)
        formsCheckbox = findViewById(R.id.formsCheckbox)
        setupButton = findViewById(R.id.setup_button)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (DATA_INSTALLED_ACTION == intent.action) {
                    showShortToast(this@SetupChecklistActivity, R.string.sync_database_updated)
                    updateState()
                } else if (CAMPAIGN_DOWNLOADED_ACTION == intent.action) {
                    showShortToast(this@SetupChecklistActivity, "Campaign downloaded")
                    updateState()
                }
            }
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        updateState()
        LocalBroadcastManager.getInstance(this).apply {
            registerReceiver(broadcastReceiver, IntentFilter(DATA_INSTALLED_ACTION))
            registerReceiver(broadcastReceiver, IntentFilter(CAMPAIGN_DOWNLOADED_ACTION))
        }
    }

    private fun updateState() {

        val hasPerms = hasPermissions().also { permissionsCheckbox.isChecked = it }
        val hasApps = hasApps().also { appsCheckbox.isChecked = it }
        val isConnected: Boolean = isAccountInstalled.also { connectCheckbox.isChecked = it }
        val hasData = hasData().also { dataCheckbox.isChecked = it }
        val hasConfig = hasConfig().also { configCheckbox.isChecked = it }
        val hasForms = hasForms().also { formsCheckbox.isChecked = it }

        fun setupButton(label: Int, action: () -> Unit) {
            setupButton.setText(label)
            setupButton.setOnClickListener { action() }
        }

        when {
            !hasPerms -> setupButton(R.string.fix_permissions) { askForPermissions(this, 1) }
            !hasApps -> setupButton(R.string.install_apps) { promptFormsAppInstall(this) }
            !isConnected -> setupButton(R.string.attach_to_server) { getToken(this, null) }
            !hasConfig -> setupButton(R.string.download_campaign) { downloadConfig(this) }
            !hasData -> setupButton(R.string.download_data) { checkForUpdate() }
            !hasForms -> setupButton(R.string.download_forms) { downloadForms(this) }
            else -> setupButton(R.string.next) { startApp(this) }
        }
    }

    private fun hasData(): Boolean = isDataAvailable(this)
    private fun hasConfig(): Boolean = isConfigAvailable
    private fun hasApps(): Boolean = isFormsAppInstalled(packageManager)
    private fun hasPermissions(): Boolean = hasRequiredPermissions(this)
    private fun hasForms(): Boolean = hasCampaignForms()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST) updateState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.configure_settings) {
            startActivity(Intent(this, PreferenceActivity::class.java))
            return true
        }
        return false
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST = 1
    }
}