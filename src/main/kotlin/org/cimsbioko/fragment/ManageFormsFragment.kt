package org.cimsbioko.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.cimsbioko.R
import org.cimsbioko.adapter.FormChecklistAdapter
import org.cimsbioko.databinding.ManageFormsFragmentBinding
import org.cimsbioko.utilities.FormsHelper.allUnsentFormInstances
import org.cimsbioko.utilities.FormsHelper.deleteFormInstances

class ManageFormsFragment : Fragment() {

    private var progressBar: ProgressBar? = null
    private var listView: ListView? = null
    private var adapter: FormChecklistAdapter? = null
    private var deleteConfirmDialog: AlertDialog? = null
    private var deleteButton: Button? = null
    private var binding: ManageFormsFragmentBinding? = null

    private var isLoading: Boolean
        get() = progressBar?.isVisible ?: false
        set(loading) {
            progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
            listView?.visibility = if (loading) View.GONE else View.VISIBLE
            deleteButton?.isEnabled = !loading
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ManageFormsFragmentBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.also {
            progressBar = it.progressBar
            listView = it.manageFormsFragmentListview
            adapter = FormChecklistAdapter(requireContext(), R.id.form_instance_check_item_orange, ArrayList())
            deleteButton = it.manageFormsFragmentPrimaryButton
            deleteConfirmDialog = AlertDialog.Builder(requireActivity())
                .setMessage(R.string.delete_forms_dialog_warning)
                .setTitle(R.string.delete_dialog_warning_title)
                .setPositiveButton(R.string.delete_forms) { _: DialogInterface?, _: Int -> deleteSelected() }
                .setNegativeButton(R.string.cancel_label, null)
                .create()
            it.manageFormsFragmentListview.adapter = adapter
            it.manageFormsFragmentListviewHeader.setText(R.string.unsent_forms)
            it.manageFormsFragmentPrimaryButton.apply {
                setOnClickListener(ButtonListener())
                setText(R.string.delete_button_label)
                tag = R.string.delete_button_label
                visibility = View.VISIBLE
            }
        }
        lifecycleScope.launch { updateForms() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        progressBar = null
        listView = null
        adapter = null
        deleteButton = null
        deleteConfirmDialog = null
    }

    private fun updateForms() {
        isLoading = true
        adapter?.clear()
        lifecycleScope.launch {
            allUnsentFormInstances.map { instances ->
                instances.map { instance -> async(Dispatchers.IO) { instance.load() } }.awaitAll()
            }.onEach { loaded ->
                adapter?.addAll(loaded)
            }.onCompletion {
                isLoading = false
            }.collect()
        }
    }

    private fun deleteSelected() {
        adapter?.checkedInstances?.also { selected ->
            deleteFormInstances(selected).also { deleted ->
                if (deleted != selected.size) {
                    Log.w(TAG, "wrong number of forms deleted: expected ${selected.size}, got $deleted")
                }
            }
            adapter?.removeAll(selected)
        }
    }

    private inner class ButtonListener : View.OnClickListener {
        override fun onClick(v: View) {
            if (adapter?.checkedInstances?.isNotEmpty() == true) deleteConfirmDialog?.show()
        }
    }

    companion object {
        private val TAG = ManageFormsFragment::class.java.simpleName
    }

}