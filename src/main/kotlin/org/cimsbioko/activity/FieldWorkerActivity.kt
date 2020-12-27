package org.cimsbioko.activity

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import org.cimsbioko.R
import org.cimsbioko.databinding.FieldworkerActivityBinding
import org.cimsbioko.search.Utils.isSearchEnabled
import org.cimsbioko.utilities.LoginUtils.login

class FieldWorkerActivity : AppCompatActivity() {

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

    companion object {
        const val ACTIVITY_MODULE_EXTRA = "ACTIVITY_MODULE_EXTRA"
    }
}