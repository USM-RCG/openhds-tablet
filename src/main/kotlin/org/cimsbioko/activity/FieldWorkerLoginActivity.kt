package org.cimsbioko.activity

import android.accounts.AccountManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.navigation.NavigationView
import org.cimsbioko.R
import org.cimsbioko.campaign.CampaignUpdateService
import org.cimsbioko.campaign.CampaignUpdateService.Companion.enqueueWork
import org.cimsbioko.databinding.FieldworkerLoginActivityBinding
import org.cimsbioko.databinding.NavHeaderBinding
import org.cimsbioko.fragment.AdminSecretFragment
import org.cimsbioko.navconfig.NavigatorConfig.Companion.instance
import org.cimsbioko.search.IndexingService.Companion.queueFullReindex
import org.cimsbioko.search.Utils.isSearchEnabled
import org.cimsbioko.syncadpt.Constants.ACCOUNT_TYPE
import org.cimsbioko.utilities.CampaignUtils.updateCampaign
import org.cimsbioko.utilities.ConfigUtils.getAppFullName
import org.cimsbioko.utilities.MessageUtils.showShortToast
import org.cimsbioko.utilities.SetupUtils.downloadForms

class FieldWorkerLoginActivity
    : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdminSecretFragment.Listener {

    private lateinit var navView: NavigationView
    private lateinit var updatePrompter: UpdatePrompter
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FieldworkerLoginActivityBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        title = getAppFullName(this)
        navView = binding.fieldworkerLoginNavigationView.apply {
            setNavigationItemSelectedListener(this@FieldWorkerLoginActivity)
        }
        toolbar = binding.fieldworkerLoginToolbar.also { setSupportActionBar(it) }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        drawerLayout = binding.fieldworkerLoginDrawerLayout.also { dl ->
            dl.addDrawerListener(DrawerToggle(dl).also { it.syncState() })
        }
        NavHeaderBinding.bind(navView.getHeaderView(0))
                .navHeaderText
                .apply {
                    AccountManager.get(this@FieldWorkerLoginActivity)
                            .getAccountsByType(ACCOUNT_TYPE)
                            .firstOrNull()
                            .let { account ->
                                text = account?.name ?: getString(R.string.app_name)
                                visibility = if (account != null) View.VISIBLE else View.GONE
                            }
                }
        updatePrompter = UpdatePrompter()
        addCrashMenuItem()
    }

    private fun addCrashMenuItem() {
        navView.menu.let { menu ->
            menu.add("Crash App").also { item ->
                item.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_adb)
                item.setOnMenuItemClickListener { throw RuntimeException("App Crash Test") }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updatePrompter)
    }

    override fun onResume() {
        super.onResume()
        navView.menu.findItem(R.id.rebuild_search_indices).isVisible = isSearchEnabled(this)
        LocalBroadcastManager.getInstance(this).apply {
            registerReceiver(updatePrompter, IntentFilter(CampaignUpdateService.CAMPAIGN_UPDATE_AVAILABLE))
        }
        enqueueWork(applicationContext, Intent(this, CampaignUpdateService::class.java))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.manage_forms -> {
            startActivity(Intent(this, ManageFormsActivity::class.java))
            true
        }
        R.id.send_forms -> {
            if (instance.adminSecret != null) {
                AdminSecretFragment().show(supportFragmentManager, "AdminSecretFragment")
            } else {
                launchSendForms()
            }
            true
        }
        R.id.download_data -> {
            startActivity(Intent(this, SyncDbActivity::class.java))
            true
        }
        R.id.rebuild_search_indices -> {
            queueFullReindex(this)
            true
        }
        R.id.download_forms -> {
            launchDownloadForms()
            true
        }
        R.id.configure_settings -> {
            startActivity(Intent(this, PreferenceActivity::class.java))
            true
        }
        else -> false
    }

    private fun launchSendForms() {
        startActivity(Intent(Intent.ACTION_EDIT))
    }

    private fun launchDownloadForms() {
        downloadForms(this)
    }

    override fun onAdminSecretDialogOk(secret: String) {
        if (secret == instance.adminSecret) launchSendForms() else showShortToast(this, R.string.invalid_admin_password)
    }

    override fun onAdminSecretDialogCancel() {}


    private inner class DrawerToggle(drawerLayout: DrawerLayout) : ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0) {

        private var tryHide = true

        override fun onDrawerOpened(drawerView: View) {
            tryHide = true
            super.onDrawerOpened(drawerView)
        }

        override fun onDrawerClosed(drawerView: View) {
            tryHide = true
            super.onDrawerClosed(drawerView)
        }

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            if (tryHide) {
                currentFocus?.let {
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(it.windowToken, 0)
                    tryHide = false
                }
            }
            super.onDrawerSlide(drawerView, slideOffset)
        }
    }

    private inner class UpdatePrompter : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.takeIf { it.action == CampaignUpdateService.CAMPAIGN_UPDATE_AVAILABLE }?.apply {
                val campaignChanged = getBooleanExtra("campaignChanged", false)
                val campaignId = getStringExtra("campaignId")
                AlertDialog.Builder(this@FieldWorkerLoginActivity)
                        .setTitle(R.string.campaign_update_title)
                        .setMessage(R.string.campaign_update_msg)
                        .setNegativeButton(R.string.no_btn) { _, _ -> }
                        .setPositiveButton(R.string.yes_btn) { _, _ -> updateCampaign(campaignId, campaignChanged) }
                        .show()
            }
        }
    }
}