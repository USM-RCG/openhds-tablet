package org.cimsbioko.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cimsbioko.activity.FieldWorkerActivity
import org.cimsbioko.activity.HierarchyNavigatorActivity
import org.cimsbioko.data.DataWrapper
import org.cimsbioko.model.FieldWorker
import org.cimsbioko.model.Form
import org.cimsbioko.model.FormInstance
import org.cimsbioko.model.HierarchyItem
import org.cimsbioko.navconfig.*
import org.cimsbioko.provider.DatabaseAdapter
import org.cimsbioko.utilities.FormUtils
import org.cimsbioko.utilities.LoginUtils
import org.cimsbioko.utilities.MessageUtils
import org.cimsbioko.utilities.logTime
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
 *   * item formatter at path
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

    val hierarchyPath = MutableStateFlow(savedStateHandle[HierarchyNavigatorActivity.HIERARCHY_PATH_KEY] ?: HierarchyPath())

    val selection: DataWrapper?
        get() = hierarchyPath.value[level]

    val childItems = MutableStateFlow<List<HierarchyItem>>(emptyList())

    private val pathHistory = Stack<HierarchyPath>()

    val selectionFormatter: ItemFormatter?
        get() = currentModule.getItemFormatter(level)

    val childItemFormatter: HierFormatter?
        get() = currentModule.getHierFormatter(level)

    val launchContext = object : LaunchContext {
        override val currentFieldWorker: FieldWorker?
            get() = LoginUtils.login.authenticatedUser
        override val currentSelection: DataWrapper?
            get() = this@NavModel.hierarchyPath.value[this@NavModel.level]
        override val hierarchyPath: HierarchyPath
            get() = this@NavModel.hierarchyPath.value
    }

    init {
        updatePath(hierarchyPath.value)
    }

    private fun updatePath(path: HierarchyPath) {
        viewModelScope.launch(Dispatchers.IO) {
            hierarchyPath.value = path
            updateChildItems()
            detailsToggleShown.value = selectionFormatter != null && childItems.value.isNotEmpty()
            itemDetailsShown.value = selectionFormatter != null && childItems.value.isEmpty()
        }
    }

    private suspend fun updateChildItems() {
        childItems.value =
                if (ROOT_LEVEL == level) config.topLevel?.let { withContext(Dispatchers.IO) { queryHelper.getAll(it)?.list } }
                else {
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
        logTime("push history") { pushHistory() }
        val newPath = logTime("clone path") { hierarchyPath.value.clone() }
        logTime("down") { newPath.down(selected.category, selected) }
        logTime("update path") { updatePath(newPath) }
    }

    val detailsToggleShown = MutableStateFlow(false)

    fun toggleDetail() {
        itemDetailsShown.value = !itemDetailsShown.value
    }

    fun processNewFormResult(formInstanceUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            FormInstance.lookup(formInstanceUri)?.also { instance ->
                DatabaseAdapter.attachFormToHierarchy(hierarchyPath.value.toString(), instance.id)
                if (instance.isComplete) {
                    try {
                        val loadedInstance = instance.load()
                        val dataDoc = loadedInstance.document
                        FormInstance.getBinding(dataDoc)?.let { binding ->
                            if (binding.consumer.consume(dataDoc, launchContext)) {
                                try {
                                    loadedInstance.store(dataDoc)
                                } catch (ue: IOException) {
                                    withContext(Dispatchers.Main) {
                                        MessageUtils.showShortToast(getApplication(), "Update failed: " + ue.message)
                                    }
                                }
                            }
                        }
                        updateChildItems()
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { MessageUtils.showShortToast(getApplication(), "Read failed: " + e.message) }
                    }
                }
            }

        }
    }

    fun generateFormInstance(binding: Binding): Intent {
        val form = Form.lookup(binding)
        val instanceUri = FormInstance.generate(form, binding, launchContext)
        return FormUtils.editIntent(form.uri).apply {
            clipData = ClipData("generated form instance", arrayOf("application/xml"), ClipData.Item(instanceUri))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
    }

    val itemDetailsShown = MutableStateFlow(false)

    companion object {
        private const val ROOT_LEVEL = "root"
    }
}