package org.cimsbioko.activity

import android.app.Activity
import android.app.SearchManager
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.cimsbioko.R
import org.cimsbioko.data.DataWrapper
import org.cimsbioko.fragment.DataSelectionFragment
import org.cimsbioko.fragment.DataSelectionFragment.DataSelectionListener
import org.cimsbioko.fragment.FormSelectionFragment
import org.cimsbioko.fragment.FormSelectionFragment.FormSelectionListener
import org.cimsbioko.fragment.navigate.DetailToggleFragment
import org.cimsbioko.fragment.navigate.DetailToggleFragment.DetailToggleListener
import org.cimsbioko.fragment.navigate.GenericDetailFragment
import org.cimsbioko.fragment.navigate.HierarchyButtonFragment
import org.cimsbioko.fragment.navigate.HierarchyButtonFragment.HierarchyButtonListener
import org.cimsbioko.fragment.navigate.HierarchyFormsFragment
import org.cimsbioko.model.core.FieldWorker
import org.cimsbioko.model.core.HierarchyItem
import org.cimsbioko.model.form.Form.Companion.lookup
import org.cimsbioko.model.form.FormInstance.Companion.generate
import org.cimsbioko.model.form.FormInstance.Companion.getBinding
import org.cimsbioko.model.form.FormInstance.Companion.lookup
import org.cimsbioko.navconfig.*
import org.cimsbioko.navconfig.db.DefaultQueryHelper
import org.cimsbioko.navconfig.db.QueryHelper
import org.cimsbioko.navconfig.forms.Binding
import org.cimsbioko.navconfig.forms.LaunchContext
import org.cimsbioko.provider.DatabaseAdapter
import org.cimsbioko.search.Utils.isSearchEnabled
import org.cimsbioko.utilities.ConfigUtils.getActiveModules
import org.cimsbioko.utilities.FormUtils.editIntent
import org.cimsbioko.utilities.LoginUtils.login
import org.cimsbioko.utilities.MessageUtils.showShortToast
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

class HierarchyNavigatorActivity : AppCompatActivity(), LaunchContext, HierarchyButtonListener, DetailToggleListener,
        DataSelectionListener, FormSelectionListener, CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var hierarchyButtonFragment: HierarchyButtonFragment
    private lateinit var valueFragment: DataSelectionFragment
    private lateinit var formFragment: FormSelectionFragment
    private lateinit var detailToggleFragment: DetailToggleFragment
    private lateinit var detailFragment: GenericDetailFragment
    private lateinit var formsFragment: HierarchyFormsFragment

    private lateinit var config: NavigatorConfig
    private lateinit var currentModuleName: String
    private lateinit var currentModule: NavigatorModule

    private lateinit var pathHistory: Stack<HierarchyPath>
    private lateinit var menuItemTags: HashMap<MenuItem, String>
    private lateinit var queryHelper: QueryHelper

    private var currentResults: List<HierarchyItem> = emptyList()
    override var hierarchyPath: HierarchyPath = HierarchyPath()
        private set

    private var formResult: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigate_activity)

        config = NavigatorConfig.instance
        currentModuleName = intent.extras?.let { it[FieldWorkerActivity.ACTIVITY_MODULE_EXTRA] as String }
                ?: "unspecified"
        currentModule = config.getModule(currentModuleName) ?: error("no module for name $currentModuleName")
        title = currentModule.activityTitle

        findViewById<Toolbar>(R.id.navigate_toolbar)?.also { setSupportActionBar(it) }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        queryHelper = DefaultQueryHelper
        hierarchyPath = HierarchyPath()

        val fragmentManager = supportFragmentManager

        hierarchyButtonFragment = fragmentManager.findFragmentById(R.id.hierarchy_button_fragment) as HierarchyButtonFragment
        detailToggleFragment = fragmentManager.findFragmentById(R.id.detail_toggle_fragment) as DetailToggleFragment
        formFragment = fragmentManager.findFragmentById(R.id.form_selection_fragment) as FormSelectionFragment
        formsFragment = fragmentManager.findFragmentById(R.id.form_list_fragment) as HierarchyFormsFragment

        detailFragment = GenericDetailFragment()
        valueFragment = DataSelectionFragment()

        if (savedInstanceState == null) {
            pathHistory = Stack()
            intent.getParcelableExtra<HierarchyPath?>(HIERARCHY_PATH_KEY)?.also {
                hierarchyPath = it
            }
            fragmentManager.beginTransaction()
                    .add(R.id.middle_column_data, valueFragment, VALUE_FRAGMENT_TAG)
                    .commit()
        } else {
            with(savedInstanceState) {
                hierarchyPath = getParcelable(HIERARCHY_PATH_KEY) ?: HierarchyPath()
                pathHistory = getSerializable(HISTORY_KEY)?.let { it as Stack<HierarchyPath> } ?: Stack()
                fragmentManager.findFragmentByTag(VALUE_FRAGMENT_TAG)?.also { valueFragment = it as DataSelectionFragment }
                fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG)?.also { detailFragment = it as GenericDetailFragment }
            }
        }
    }

    override fun onResume() {
        job = Job()
        super.onResume()
    }

    override fun onPostResume() {
        super.onPostResume()
        update()  // allow fragments to resume before updating
    }

    override fun onPause() {
        job.cancel("activity paused")
        super.onPause()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState.apply {
            putParcelable(HIERARCHY_PATH_KEY, hierarchyPath)
            putSerializable(HISTORY_KEY, pathHistory)
        })
    }

    /*
     * A hack to inject extra context when starting the search activity, onSearchRequested was not being called.
     */
    override fun startActivity(intent: Intent) {
        intent.takeIf { it.action == Intent.ACTION_SEARCH }
                ?.putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, currentModuleName)
        super.startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.fieldworker_menu, menu)
        menuItemTags = HashMap()
        getActiveModules(this).filter { it.name != currentModuleName }.forEach { module ->
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
                    putExtra(HIERARCHY_PATH_KEY, hierarchyPath)
                })
            } ?: return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun launchNewForm(binding: Binding?) {
        binding?.also { it ->
            try {
                showShortToast(this, R.string.launching_form)
                val form = lookup(it)
                val instanceUri = generate(form, it, this)
                startActivityForResult(editIntent(form.uri).apply {
                    clipData = ClipData("generated form instance", arrayOf("application/xml"), ClipData.Item(instanceUri))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                }, FORM_ACTIVITY_REQUEST_CODE)
            } catch (e: Exception) {
                showShortToast(this, "failed to launch form: " + e.message)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.also {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FORM_ACTIVITY_REQUEST_CODE) {
                    formResult = it.data
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Handles forms created with launchNewForm on return from the forms app.
     */
    private suspend fun handleFormResultIfPresent() {
        formResult?.also { uri ->
            formResult = null
            withContext(Dispatchers.IO) {
                lookup(uri)?.also { instance ->
                    DatabaseAdapter.attachFormToHierarchy(hierarchyPath.toString(), instance.id)
                    if (instance.isComplete) {
                        val activity = this@HierarchyNavigatorActivity
                        try {
                            val loadedInstance = instance.load()
                            val dataDoc = loadedInstance.document
                            getBinding(dataDoc)?.let { binding ->
                                if (binding.consumer.consume(dataDoc, activity)) {
                                    try {
                                        loadedInstance.store(dataDoc)
                                    } catch (ue: IOException) {
                                        withContext(Dispatchers.Main) {
                                            showShortToast(activity, "Update failed: " + ue.message)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) { showShortToast(activity, "Read failed: " + e.message) }
                        }
                    }
                }
            }
        }
    }

    override val currentSelection: DataWrapper?
        get() = hierarchyPath[level]

    override val currentFieldWorker: FieldWorker?
        get() = login.authenticatedUser

    override fun onFormSelected(binding: Binding) {
        launchNewForm(binding)
    }

    override fun onDataSelected(data: DataWrapper) {
        stepDown(data)
    }

    override fun onDetailToggled() {
        launch {
            if (valueFragment.isAdded) {
                showDetailFragment()
                detailToggleFragment.setDetailsShown(true)
            } else if (detailFragment.isAdded) {
                showValueFragment()
                detailToggleFragment.setDetailsShown(false)
            }
        }
    }

    private fun showValueFragment() {
        // there is only 1 value fragment that can be added
        if (!valueFragment.isAdded) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.middle_column_data, valueFragment, VALUE_FRAGMENT_TAG)
                    .commit()
            supportFragmentManager.executePendingTransactions()
        }
        hierFormatterForCurrentLevel?.also { valueFragment.populateData(currentResults, it) }
    }

    private suspend fun showDetailFragment() {
        detailFragment = GenericDetailFragment().also { fragment ->
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.middle_column_data, fragment, DETAIL_FRAGMENT_TAG)
                    .commit()
            supportFragmentManager.executePendingTransactions()
            withContext(Dispatchers.IO) { currentSelection?.unwrapped }
                    ?.let { itemFormatterForCurrentLevel?.format(it) }
                    ?.also { fragment.showItemDetails(it) }
        }
    }

    private val itemFormatterForCurrentLevel: ItemFormatter?
        get() = currentModule.getItemFormatter(level)

    private val hierFormatterForCurrentLevel: HierFormatter?
        get() = currentModule.getHierFormatter(level)

    override fun onHierarchyButtonClicked(level: String) = jumpUp(level)

    private fun jumpUp(level: String) {
        val isRootLevel = ROOT_LEVEL == level
        check(isRootLevel || hierarchyPath.levels.contains(level)) { "invalid level: $level" }
        pushHistory()
        if (isRootLevel) {
            hierarchyPath.clear()
        } else {
            hierarchyPath.truncate(level)
        }
        update()
    }

    private fun stepDown(selected: DataWrapper) {
        pushHistory()
        hierarchyPath.down(selected.category, selected)
        update()
    }

    private fun pushHistory() {
        pathHistory.push(hierarchyPath.clone())
    }

    override fun onBackPressed() {
        if (!pathHistory.empty()) {
            hierarchyPath = pathHistory.pop()
            update()
        } else {
            super.onBackPressed()
        }
    }

    private val level: String
        get() = if (hierarchyPath.depth() <= 0) ROOT_LEVEL else config.levels[hierarchyPath.depth() - 1]

    private fun update() = launch {
        val level = level
        check(ROOT_LEVEL == level || level in config.levels) { "no such level: $level" }
        handleFormResultIfPresent()
        updatePathButtons()
        updateData()
        updateMiddle()
        updateDetailToggle()
        updateFormLaunchers()
        updateForms()
    }

    private fun updatePathButtons() {
        hierarchyButtonFragment.update(hierarchyPath)
    }

    private suspend fun updateData() {
        currentResults =
                if (ROOT_LEVEL == level) config.topLevel?.let { withContext(Dispatchers.IO) { queryHelper.getAll(it)?.list } }
                else {
                    hierarchyPath.depth()
                            .takeIf { it in config.levels.indices }
                            ?.let { depth -> config.levels[depth] }
                            ?.let { nextLevel ->
                                currentSelection?.let { currentItem ->
                                    withContext(Dispatchers.IO) {
                                        queryHelper.getChildren(parent = currentItem, childLevel = nextLevel)?.list
                                    }
                                }
                            }
                } ?: emptyList()
    }

    private suspend fun updateMiddle() = if (shouldShowDetail()) showDetailFragment() else showValueFragment()

    private fun updateDetailToggle() {
        detailToggleFragment.apply {
            if (itemFormatterForCurrentLevel != null && !shouldShowDetail()) {
                setEnabled(true)
                if (!valueFragment.isAdded) setDetailsShown(true)
            } else setEnabled(false)
        }
    }

    private fun shouldShowDetail(): Boolean = itemFormatterForCurrentLevel != null && currentResults.isEmpty()

    private fun updateFormLaunchers() {
        currentModule.getLaunchers(level)
                .filter { it.relevantFor(this@HierarchyNavigatorActivity) }
                .let { relevantLaunchers ->
                    formFragment.createFormButtons(relevantLaunchers)
                }
    }

    /**
     * Refreshes the attached forms at the current hierarchy path and prunes sent form associations.
     */
    private suspend fun updateForms() {
        formsFragment.path = hierarchyPath
        withContext(Dispatchers.IO) {
            DatabaseAdapter.findFormsForHierarchy(hierarchyPath.toString())
                    .filter { it.isSubmitted }
                    .map { it.id }
                    .toList()
                    .let { idList -> DatabaseAdapter.detachFromHierarchy(idList) }
        }
    }

    companion object {
        private const val FORM_ACTIVITY_REQUEST_CODE = 0
        private const val VALUE_FRAGMENT_TAG = "hierarchyValueFragment"
        private const val DETAIL_FRAGMENT_TAG = "hierarchyDetailFragment"
        private const val HISTORY_KEY = "navHistory"
        const val HIERARCHY_PATH_KEY = "hierarchyPathKeys"
        private const val ROOT_LEVEL = "root"
    }
}