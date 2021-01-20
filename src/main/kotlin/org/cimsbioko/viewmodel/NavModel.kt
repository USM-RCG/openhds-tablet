package org.cimsbioko.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.cimsbioko.activity.FieldWorkerActivity
import org.cimsbioko.activity.HierarchyNavigatorActivity
import org.cimsbioko.data.DataWrapper
import org.cimsbioko.model.*
import org.cimsbioko.navconfig.*
import org.cimsbioko.provider.DatabaseAdapter
import org.cimsbioko.utilities.FormUtils
import org.cimsbioko.utilities.LoginUtils
import org.cimsbioko.utilities.MessageUtils
import java.io.IOException
import java.util.*

/**
 * The model for hierarchy navigator activity and its fragments.
 *
 * The model is composed of:
 *   * level
 *   * hierarchy path
 *   * selection
 *   * child items
 *   * relevant launchers for current launch context
 *   * forms for path
 *   * hierarchy item formatter at path
 *   * child items formatter at path
 *   * detail toggle shown
 *   * show item or children shown
 */
class NavModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val queryHelper: QueryHelper = DefaultQueryHelper

    val config = NavigatorConfig.instance
    val currentModuleName: String = savedStateHandle[FieldWorkerActivity.ACTIVITY_MODULE_EXTRA] ?: "unspecified"
    val currentModule = config.getModule(currentModuleName) ?: error("no module for name $currentModuleName")

    val level: String
        get() = if (hierarchyPath.value.depth() <= 0) ROOT_LEVEL else config.levels[hierarchyPath.value.depth() - 1]

    private val isRootLevel
        get() = ROOT_LEVEL == level

    val selection: DataWrapper?
        get() = hierarchyPath.value[level]

    val hierarchyPath = MutableStateFlow(savedStateHandle[HierarchyNavigatorActivity.HIERARCHY_PATH_KEY] ?: HierarchyPath())
    val childItems = MutableStateFlow<ChildItems>(ChildItems.Loading)
    val launcherItems = MutableStateFlow<LauncherItems>(LauncherItems.Loading)
    val formItems = MutableStateFlow<FormItems>(FormItems.Loading)
    val details = MutableStateFlow<Details>(Details.Loading)
    val detailsToggleShown = MutableStateFlow(false)
    val itemDetailsShown = MutableStateFlow(false)

    private val pathHistory = Stack<HierarchyPath>()

    private val selectionFormatter: ItemFormatter?
        get() = currentModule.getItemFormatter(level)

    val childItemFormatter: HierFormatter?
        get() = currentModule.getHierFormatter(level)

    private val launchContext: LaunchContext
        get() = object : LaunchContext {
            override val currentFieldWorker: FieldWorker? = LoginUtils.login.authenticatedUser
            override val currentSelection: DataWrapper? = this@NavModel.hierarchyPath.value[this@NavModel.level]
            override val hierarchyPath: HierarchyPath = this@NavModel.hierarchyPath.value
        }

    init {
        updatePath(hierarchyPath.value)
    }

    private fun updatePath(path: HierarchyPath) {
        hierarchyPath.value = path
        updateItems()
    }

    private fun updateItems() {
        viewModelScope.launch {
            updateChildItems()
            updateLauncherItems()
            updateFormItems()
            updateDetails()
        }
    }

    private suspend fun updateDetails() {
        details.value = Details.Loading
        selectionFormatter?.also { formatter ->
            withContext(Dispatchers.IO) {
                selection?.unwrapped?.let { formatter.format(it) }
            }?.also {
                details.value = Details.Loaded(it)
            }
        }
    }

    private fun updateLauncherItems() {
        viewModelScope.launch(Dispatchers.IO) {
            launcherItems.value = LauncherItems.Loading
            currentModule.getLaunchers(level)
                .map { launcher ->
                    async { launcher.takeIf { l -> l.relevantFor(launchContext) } }
                }
                .awaitAll()
                .filterNotNull()
                .also { launcherItems.value = LauncherItems.Loaded(it) }
        }
    }

    private suspend fun updateFormItems() {
        viewModelScope.launch {
            DatabaseAdapter
                .findFormsForHierarchy(hierarchyPath.value.toString())
                .map { instances ->
                    instances
                        .filterNot { instance -> instance.isSubmitted }
                        .map { instance -> async(Dispatchers.IO) { instance.load() } }
                        .awaitAll()
                }
                .onStart { formItems.value = FormItems.Loading }
                .onEach { list -> formItems.value = FormItems.Loaded(list) }
                .collect()
        }
    }

    private suspend fun updateChildItems() {
        childItems.value = ChildItems.Loading
        detailsToggleShown.value = false
        itemDetailsShown.value = false
        getChildItems().also { children ->
            detailsToggleShown.value = selectionFormatter != null && children.isNotEmpty()
            itemDetailsShown.value = (selectionFormatter != null && children.isEmpty())
            childItems.value = ChildItems.Loaded(children)
        }
    }

    sealed class ChildItems {
        object Loading : ChildItems()
        class Loaded(val items: List<HierarchyItem>) : ChildItems()
    }

    private suspend fun getChildItems() = if (ROOT_LEVEL == level) {
        config.topLevel?.let { withContext(Dispatchers.IO) { queryHelper.getAll(it)?.list } }
    } else {
        hierarchyPath.value.depth()
            .takeIf { it in config.levels.indices }
            ?.let { depth -> config.levels[depth] }
            ?.let { nextLevel ->
                selection?.let { currentItem ->
                    withContext(Dispatchers.IO) {
                        queryHelper.getChildren(parent = currentItem, childLevel = nextLevel)?.list
                    }
                }
            }
    } ?: emptyList()

    sealed class LauncherItems {
        object Loading : LauncherItems()
        data class Loaded(val items: List<Launcher>) : LauncherItems()
    }

    sealed class FormItems {
        object Loading : FormItems()
        data class Loaded(val items: List<LoadedFormInstance>) : FormItems()
    }

    sealed class Details {
        object Loading : Details()
        data class Loaded(val details: ItemDetails) : Details()
    }

    private fun pushHistory() {
        pathHistory.push(hierarchyPath.value.clone())
    }

    fun popHistory(): Boolean = if (pathHistory.isEmpty()) false else true.also { updatePath(pathHistory.pop()) }

    fun jumpUp(level: String) {
        isRootLevel.also { isRoot ->
            if (isRoot || hierarchyPath.value.levels.contains(level)) {
                pushHistory()
                hierarchyPath.value.clone().apply {
                    if (isRoot) clear() else truncate(level)
                }.also { updatePath(it) }
            }
        }
    }

    fun stepDown(selected: DataWrapper) {
        pushHistory()
        hierarchyPath.value.clone().also { newPath ->
            newPath.down(selected.category, selected)
            updatePath(newPath)
        }
    }


    fun toggleDetail() {
        itemDetailsShown.value = !itemDetailsShown.value
    }

    fun processNewFormResult(formInstanceUri: Uri) {
        val ctx = launchContext
        viewModelScope.launch(Dispatchers.IO) {
            FormInstance.lookup(formInstanceUri)?.also { instance ->
                DatabaseAdapter.attachFormToHierarchy(hierarchyPath.value.toString(), instance.id)
                if (instance.isComplete) {
                    try {
                        val loadedInstance = instance.load()
                        val dataDoc = loadedInstance.document
                        FormInstance.getBinding(dataDoc)?.let { binding ->
                            if (binding.consumer.consume(dataDoc, ctx)) {
                                try {
                                    loadedInstance.store(dataDoc)
                                } catch (ue: IOException) {
                                    withContext(Dispatchers.Main) {
                                        MessageUtils.showShortToast(getApplication(), "Update failed: ${ue.message}")
                                    }
                                }
                            }
                        }
                        updateItems()
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            MessageUtils.showShortToast(getApplication(), "Read failed: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun generateFormInstance(binding: Binding): Intent = withContext(Dispatchers.IO) {
        val form = Form.lookup(binding)
        val instanceUri = FormInstance.generate(form, binding, launchContext)
        FormUtils.editIntent(form.uri).apply {
            clipData = ClipData("generated form instance", arrayOf("application/xml"), ClipData.Item(instanceUri))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
    }


    companion object {
        private const val ROOT_LEVEL = "root"
    }
}