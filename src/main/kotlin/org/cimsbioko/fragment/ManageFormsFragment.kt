package org.cimsbioko.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.cimsbioko.R
import org.cimsbioko.adapter.FormChecklistAdapter
import org.cimsbioko.databinding.ManageFormsFragmentBinding
import org.cimsbioko.utilities.FormsHelper.allUnsentFormInstances
import org.cimsbioko.utilities.FormsHelper.deleteFormInstances

class ManageFormsFragment : Fragment() {

    private var adapter: FormChecklistAdapter? = null
    private var deleteConfirmDialog: AlertDialog? = null

    override fun onResume() {
        updateForms()
        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ManageFormsFragmentBinding.inflate(inflater, container, false).also {
            adapter = FormChecklistAdapter(requireContext(), R.id.form_instance_check_item_orange, ArrayList())
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
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        deleteConfirmDialog = null
    }

    private fun updateForms() {
        lifecycleScope.launch { allUnsentFormInstances.map { it.load() }.flowOn(Dispatchers.IO).collect { form -> adapter?.add(form) } }
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