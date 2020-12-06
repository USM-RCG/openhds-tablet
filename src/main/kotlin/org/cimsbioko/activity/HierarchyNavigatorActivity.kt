package org.cimsbioko.activity

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.cimsbioko.R
import org.cimsbioko.databinding.NavigateActivityBinding
import org.cimsbioko.fragment.*
import org.cimsbioko.navconfig.*
import org.cimsbioko.search.Utils.isSearchEnabled
import org.cimsbioko.utilities.ConfigUtils.getActiveModules
import org.cimsbioko.utilities.LoginUtils.login
import org.cimsbioko.viewmodel.NavModel
import org.cimsbioko.viewmodel.NavModelFactory
import java.util.*

class HierarchyNavigatorActivity : AppCompatActivity() {

    private val model: NavModel by viewModels { NavModelFactory(this, intent.extras) }

    private lateinit var hierarchyButtonFragment: HierarchyButtonFragment
    private lateinit var valueFragment: DataSelectionFragment
    private lateinit var formFragment: FormSelectionFragment
    private lateinit var detailToggleFragment: DetailToggleFragment
    private lateinit var detailFragment: GenericDetailFragment
    private lateinit var formsFragment: HierarchyFormsFragment

    private lateinit var menuItemTags: HashMap<MenuItem, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = NavigateActivityBinding.inflate(layoutInflater).apply { setContentView(root) }

        title = model.currentModule.activityTitle
        setSupportActionBar(binding.navigateToolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        val fragmentManager = supportFragmentManager

        hierarchyButtonFragment = fragmentManager.findFragmentById(R.id.hierarchy_button_fragment) as HierarchyButtonFragment
        detailToggleFragment = fragmentManager.findFragmentById(R.id.detail_toggle_fragment) as DetailToggleFragment
        formFragment = fragmentManager.findFragmentById(R.id.form_selection_fragment) as FormSelectionFragment
        formsFragment = fragmentManager.findFragmentById(R.id.form_list_fragment) as HierarchyFormsFragment

        detailFragment = GenericDetailFragment()
        valueFragment = DataSelectionFragment()

        if (savedInstanceState != null) {
            fragmentManager.findFragmentByTag(VALUE_FRAGMENT_TAG)?.also { valueFragment = it as DataSelectionFragment }
            fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG)?.also { detailFragment = it as GenericDetailFragment }
        } else {
            fragmentManager.beginTransaction()
                    .add(R.id.middle_column_data, valueFragment, VALUE_FRAGMENT_TAG)
                    .add(R.id.middle_column_data, detailFragment, DETAIL_FRAGMENT_TAG)
                    .show(if (model.itemDetailsShown.value) detailFragment else valueFragment)
                    .hide(if (model.itemDetailsShown.value) valueFragment else detailFragment)
                    .commitNow()
        }

        model.itemDetailsShown.onEach { showingDetails ->
            supportFragmentManager
                    .beginTransaction()
                    .show(if (showingDetails) detailFragment else valueFragment)
                    .hide(if (showingDetails) valueFragment else detailFragment)
                    .commitNow()
        }.launchIn(lifecycleScope)
    }

    /*
     * A hack to inject extra context when starting the search activity, onSearchRequested was not being called.
     */
    override fun startActivity(intent: Intent) {
        intent.takeIf { it.action == Intent.ACTION_SEARCH }?.putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, model.currentModuleName)
        super.startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.fieldworker_menu, menu)
        menuItemTags = HashMap()
        getActiveModules(this).filter { it.name != model.currentModuleName }.forEach { module ->
            menu.add(module.activityTitle).also { item ->
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                menuItemTags[item] = module.name
            }
        }
        val searchMenuItem = menu.findItem(R.id.field_worker_search)
        val searchEnabled = isSearchEnabled(this)
        if (searchEnabled) {
            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            val searchInfo = searchManager.getSearchableInfo(ComponentName(this, SearchableActivity::class.java))
            val searchView = searchMenuItem.actionView as SearchView
            searchView.setSearchableInfo(searchInfo)
        }
        searchMenuItem.isVisible = searchEnabled
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout_menu_button) {
            login.logout(this, true)
        } else {
            menuItemTags[item]?.also { menuModule ->
                startActivity(Intent().apply {
                    setClass(this@HierarchyNavigatorActivity, HierarchyNavigatorActivity::class.java)
                    putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, menuModule)
                    putExtra(HIERARCHY_PATH_KEY, model.hierarchyPath.value)
                })
            } ?: return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (!model.popHistory()) {
            super.onBackPressed()
        }
    }

    companion object {
        private const val VALUE_FRAGMENT_TAG = "hierarchyValueFragment"
        private const val DETAIL_FRAGMENT_TAG = "hierarchyDetailFragment"
        const val HIERARCHY_PATH_KEY = "hierarchyPathKeys"
    }
}