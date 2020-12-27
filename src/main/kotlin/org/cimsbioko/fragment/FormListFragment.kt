package org.cimsbioko.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.cimsbioko.R
import org.cimsbioko.activity.FieldWorkerActivity
import org.cimsbioko.activity.HierarchyNavigatorActivity
import org.cimsbioko.adapter.FormInstanceAdapter
import org.cimsbioko.databinding.FormListFragmentBinding
import org.cimsbioko.model.FormInstance
import org.cimsbioko.model.FormInstance.Companion.getBinding
import org.cimsbioko.model.LoadedFormInstance
import org.cimsbioko.navconfig.HierarchyPath.Companion.fromString
import org.cimsbioko.provider.DatabaseAdapter
import org.cimsbioko.utilities.ConfigUtils.getActiveModuleForBinding
import org.cimsbioko.utilities.ConfigUtils.getActiveModules
import org.cimsbioko.utilities.FormUtils.editIntent
import org.cimsbioko.utilities.FormsHelper
import org.cimsbioko.utilities.FormsHelper.deleteFormInstances
import org.cimsbioko.utilities.MessageUtils.showShortToast
import org.cimsbioko.viewmodel.NavModel
import java.util.*

abstract class FormListFragment : Fragment() {

    private var headerView: TextView? = null
    private var list: ListView? = null
    private var progress: ProgressBar? = null
    protected var dataAdapter: FormInstanceAdapter? = null

    var isFindEnabled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FormListFragmentBinding.inflate(inflater, container, false).also { binding ->
            headerView = binding.formListHeader
            dataAdapter = FormInstanceAdapter(requireActivity(), R.id.form_instance_list_item, ArrayList())
            list = binding.formList
            progress = binding.progressBar
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list?.apply {
            adapter = dataAdapter
            onItemClickListener = ClickListener()
            registerForContextMenu(this)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        headerView = null
        list?.let { unregisterForContextMenu(it) }
        list = null
        dataAdapter = null
        progress = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FIND_ENABLED_KEY, isFindEnabled)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply { isFindEnabled = getBoolean(FIND_ENABLED_KEY) }
    }

    private var isLoading: Boolean
        get() = progress?.isVisible ?: false
        set(loading) {
            progress?.visibility = if (loading) VISIBLE else GONE
            list?.visibility = if (loading) GONE else VISIBLE
        }

    fun setHeaderText(resourceId: Int?) {
        if (resourceId != null) {
            headerView?.setText(resourceId)
            headerView?.visibility = VISIBLE
        } else {
            headerView?.visibility = GONE
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    protected fun populate(instances: Flow<List<FormInstance>>) {
        isLoading = true
        dataAdapter?.clear()
        lifecycleScope.launch {
            instances.map { list ->
                list.map { item -> async(Dispatchers.IO) { item.load() } }.awaitAll()
            }.onEach { loaded ->
                dataAdapter?.addAll(loaded)
            }.onCompletion {
                isLoading = false
            }.collect()
        }
    }

    private fun getItem(pos: Int): LoadedFormInstance {
        return list?.getItemAtPosition(pos) as LoadedFormInstance // accounts for offset shifts from added headers
    }

    private fun editForm(selected: FormInstance) {
        if (selected.isEditable) {
            showShortToast(activity, R.string.launching_form)
            startActivityForResult(editIntent(selected.uri), 0)
        } else {
            showShortToast(activity, R.string.form_not_editable)
        }
    }

    private fun removeForm(selected: LoadedFormInstance) {
        if (deleteFormInstances(listOf(selected)) == 1) {
            dataAdapter?.remove(selected)
            showShortToast(activity, R.string.deleted)
        }
    }

    private fun findForm(selected: LoadedFormInstance) {
        val ctx = requireContext()
        fromString(DatabaseAdapter.findHierarchyForForm(selected.id))?.let { path ->
            try {
                // launch the navigator using the first relevant module
                (getBinding(selected.document)
                        ?.let { getActiveModuleForBinding(ctx, it) }
                        ?.takeIf { it.isNotEmpty() } ?: getActiveModules(ctx))
                        .firstOrNull()?.also { firstModule ->
                            startActivity(Intent(ctx, HierarchyNavigatorActivity::class.java).apply {
                                putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, firstModule.name)
                                putExtra(HierarchyNavigatorActivity.HIERARCHY_PATH_KEY, path)
                            })
                        } ?: showShortToast(ctx, R.string.no_active_modules)

            } catch (e: Exception) {
                showShortToast(ctx, R.string.form_load_failed)
            }
        } ?: showShortToast(ctx, R.string.form_not_found)
    }

    private fun confirmDelete(selected: LoadedFormInstance) {
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.delete_forms_dialog_warning)
                .setTitle(R.string.delete_dialog_warning_title)
                .setPositiveButton(R.string.delete_forms) { _: DialogInterface?, _: Int -> removeForm(selected) }
                .setNegativeButton(R.string.cancel_label, null)
                .create()
                .show()
    }

    private inner class ClickListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) = editForm(getItem(position))
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        if (v.id == R.id.form_list) {
            requireActivity().menuInflater.inflate(R.menu.formlist_menu, menu)
            (menuInfo as? AdapterContextMenuInfo)?.let { getItem(it.position) }?.also { selected ->
                menu.findItem(R.id.edit_form).isVisible = selected.isEditable
            }
            menu.findItem(R.id.find_form).isVisible = isFindEnabled
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        return when (item.itemId) {
            R.id.find_form -> {
                findForm(getItem(info.position))
                true
            }
            R.id.delete_form -> {
                confirmDelete(getItem(info.position))
                true
            }
            R.id.edit_form -> {
                editForm(getItem(info.position))
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    companion object {
        private const val FIND_ENABLED_KEY = "FIND_ENABLED"
    }

}

class UnsentFormsFragment : FormListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            setHeaderText(R.string.unsent_forms)
            isFindEnabled = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch { populate(FormsHelper.allUnsentFormInstances) }
    }
}

class HierarchyFormsFragment : FormListFragment() {

    private val model: NavModel by activityViewModels()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.hierarchyPath
            .mapLatest { path ->
                DatabaseAdapter.findFormsForHierarchy(path.toString()).map { instances ->
                    instances.filterNot { instance -> instance.isSubmitted }
                }
            }.onEach { populate(it) }
            .launchIn(lifecycleScope)
    }
}