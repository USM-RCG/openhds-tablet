package org.cimsbioko.activity

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.cimsbioko.R
import org.cimsbioko.databinding.FieldworkerActivityBinding
import org.cimsbioko.search.Utils.isSearchEnabled
import org.cimsbioko.utilities.ConfigUtils.getActiveModules
import org.cimsbioko.utilities.LoginUtils.login
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText

class FieldWorkerActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FieldworkerActivityBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        title = getText(R.string.field_worker_home)
        setSupportActionBar(binding.fieldworkerToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        lifecycleScope.launch {
            val activity = this@FieldWorkerActivity
            val activitiesLayout = binding.portalMiddleColumn
            getActiveModules(activity).also { modules ->
                val lastIndex = modules.indices.last
                for ((index, module) in modules.withIndex()) {
                    makeText(
                        activity,
                        layoutTag = module.name,
                        listener = activity,
                        container = activitiesLayout,
                        background = R.drawable.data_selector
                    ).apply {
                        configureText(
                            activity,
                            text1 = module.launchLabel,
                            text2 = module.launchDescription
                        )
                    }.takeIf { index != lastIndex }
                        ?.root
                        ?.let { it.layoutParams as? LinearLayout.LayoutParams }
                        ?.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.module_button_spacing))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.fieldworker_menu, menu)
        menu.findItem(R.id.field_worker_search).isVisible = isSearchEnabled(this).also { enabled ->
            if (enabled) {
                val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
                val searchView = menu.findItem(R.id.field_worker_search).actionView as SearchView
                val searchInfo = searchManager.getSearchableInfo(ComponentName(this, SearchableActivity::class.java))
                searchView.setSearchableInfo(searchInfo)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout_menu_button) {
            login.logout(this, true)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        startActivity(Intent(this, HierarchyNavigatorActivity::class.java).apply {
            putExtra(ACTIVITY_MODULE_EXTRA, v.tag as String)
        })
    }

    companion object {
        const val ACTIVITY_MODULE_EXTRA = "ACTIVITY_MODULE_EXTRA"
    }
}