package org.cimsbioko.fragment.navigate

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.activity.FieldWorkerActivity
import org.cimsbioko.activity.HierarchyNavigatorActivity
import org.cimsbioko.adapter.FormInstanceAdapter
import org.cimsbioko.model.form.FormInstance
import org.cimsbioko.model.form.FormInstance.Companion.getBinding
import org.cimsbioko.navconfig.HierarchyPath.Companion.fromString
import org.cimsbioko.provider.DatabaseAdapter.Companion.instance
import org.cimsbioko.utilities.ConfigUtils.getActiveModuleForBinding
import org.cimsbioko.utilities.ConfigUtils.getActiveModules
import org.cimsbioko.utilities.FormUtils.editIntent
import org.cimsbioko.utilities.FormsHelper.deleteFormInstances
import org.cimsbioko.utilities.MessageUtils.showShortToast
import java.util.*

class FormListFragment : Fragment() {

    private lateinit var headerView: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: FormInstanceAdapter

    var isFindEnabled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.form_list_fragment, container, false).also { view ->
            headerView = view.findViewById(R.id.form_list_header)
            adapter = FormInstanceAdapter(requireActivity(), R.id.form_instance_list_item, ArrayList())
            listView = view.findViewById<ListView>(R.id.form_list).also { listView ->
                listView.adapter = adapter
                listView.onItemClickListener = ClickListener()
                registerForContextMenu(listView)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FIND_ENABLED_KEY, isFindEnabled)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply { isFindEnabled = getBoolean(FIND_ENABLED_KEY) }
    }

    fun setHeaderText(resourceId: Int?) {
        if (resourceId != null) {
            headerView.setText(resourceId)
            headerView.visibility = View.VISIBLE
        } else {
            headerView.visibility = View.GONE
        }
    }

    fun populate(forms: List<FormInstance>) = adapter.populate(forms)

    private fun getItem(pos: Int): FormInstance {
        return listView.getItemAtPosition(pos) as FormInstance // accounts for offset shifts from added headers
    }

    private fun editForm(selected: FormInstance) {
        if (selected.isEditable) {
            showShortToast(activity, R.string.launching_form)
            startActivityForResult(editIntent(selected.uri), 0)
        } else {
            showShortToast(activity, R.string.form_not_editable)
        }
    }

    private fun removeForm(selected: FormInstance) {
        if (deleteFormInstances(listOf(selected)) == 1) {
            adapter.remove(selected)
            showShortToast(activity, R.string.deleted)
        }
    }

    private fun findForm(selected: FormInstance) {
        val ctx = requireContext()
        fromString(instance.findHierarchyForForm(selected.id))?.let { path ->
            try {
                // launch the navigator using the first relevant module
                (getBinding(selected.load())
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

    private fun confirmDelete(selected: FormInstance) {
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.delete_forms_dialog_warning)
                .setTitle(R.string.delete_dialog_warning_title)
                .setPositiveButton(R.string.delete_forms) { dialogInterface: DialogInterface?, i: Int -> removeForm(selected) }
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