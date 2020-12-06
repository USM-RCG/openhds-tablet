package org.cimsbioko.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cimsbioko.activity.FieldWorkerActivity
import org.cimsbioko.activity.HierarchyNavigatorActivity
import org.cimsbioko.data.DataWrapper
import org.cimsbioko.model.FieldWorker
import org.cimsbioko.model.HierarchyItem
import org.cimsbioko.navconfig.*
import org.cimsbioko.utilities.LoginUtils
import org.cimsbioko.utilities.logTime
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
class NavModel(savedStateHandle: SavedStateHandle) : ViewModel() {

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

    val itemDetailsShown = MutableStateFlow(false)

    companion object {
        private const val ROOT_LEVEL = "root"
    }
}

class NavModelFactory(
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T = NavModel(handle) as T
}