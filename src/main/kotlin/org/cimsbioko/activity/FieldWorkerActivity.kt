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
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import org.cimsbioko.R
import org.cimsbioko.search.Utils.isSearchEnabled
import org.cimsbioko.utilities.ConfigUtils.getActiveModules
import org.cimsbioko.utilities.LayoutUtils.makeTextWithPayload
import org.cimsbioko.utilities.LoginUtils.login

class FieldWorkerActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fieldworker_activity)
        title = getText(R.string.field_worker_home)
        findViewById<Toolbar>(R.id.fieldworker_toolbar).also { setSupportActionBar(it) }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        val activitiesLayout = findViewById<LinearLayout>(R.id.portal_middle_column)
        getActiveModules(this).also { modules ->
            val lastIndex = modules.indices.last
            for ((index, module) in modules.withIndex()) {
                makeTextWithPayload(this, module.launchLabel, module.launchDescription, module.name, this,
                        activitiesLayout, R.drawable.data_selector, null, null, true)
                        .takeIf { index != lastIndex }
                        ?.let { it.layoutParams as LinearLayout.LayoutParams }
                        ?.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.module_button_spacing))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.fieldworker_menu, menu)
        menu.findItem(R.id.field_worker_search).isVisible = isSearchEnabled(this).also { enabled ->
            if (enabled) {
                val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
                val searchView = MenuItemCompat.getActionView(menu.findItem(R.id.field_worker_search)) as SearchView
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