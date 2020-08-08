package org.cimsbioko.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.cimsbioko.R
import org.cimsbioko.adapter.FormChecklistAdapter
import org.cimsbioko.utilities.FormsHelper.allUnsentFormInstances
import org.cimsbioko.utilities.FormsHelper.deleteFormInstances
import kotlin.coroutines.CoroutineContext

class ManageFormsFragment : Fragment(), CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var adapter: FormChecklistAdapter
    private lateinit var deleteConfirmDialog: AlertDialog

    override fun onResume() {
        job = Job()
        updateForms()
        super.onResume()
    }

    override fun onPause() {
        job.cancel("fragment paused")
        super.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return (inflater.inflate(R.layout.manage_forms_fragment, container, false) as RelativeLayout).apply {

            adapter = FormChecklistAdapter(requireContext(), R.id.form_instance_check_item_orange, ArrayList())

            deleteConfirmDialog = AlertDialog.Builder(requireActivity())
                    .setMessage(R.string.delete_forms_dialog_warning)
                    .setTitle(R.string.delete_dialog_warning_title)
                    .setPositiveButton(R.string.delete_forms) { _: DialogInterface?, _: Int -> deleteSelected() }
                    .setNegativeButton(R.string.cancel_label, null)
                    .create()

            findViewById<ListView>(R.id.manage_forms_fragment_listview).adapter = adapter

            findViewById<TextView>(R.id.manage_forms_fragment_listview_header).apply {
                setText(R.string.unsent_forms)
            }

            findViewById<Button>(R.id.manage_forms_fragment_primary_button).apply {
                setOnClickListener(ButtonListener())
                setText(R.string.delete_button_label)
                tag = R.string.delete_button_label
                visibility = View.VISIBLE
            }
        }
    }

    private fun updateForms() {
        launch { allUnsentFormInstances.map { it.load() }.flowOn(Dispatchers.IO).collect { form -> adapter.add(form) } }
    }

    private fun deleteSelected() {
        adapter.removeAll(adapter.checkedInstances.also { selected ->
            deleteFormInstances(selected).also { deleted ->
                if (deleted != selected.size) {
                    Log.w(TAG, "wrong number of forms deleted: expected ${selected.size}, got $deleted")
                }
            }
        })
    }

    private inner class ButtonListener : View.OnClickListener {
        override fun onClick(v: View) {
            if (adapter.checkedInstances.isNotEmpty()) deleteConfirmDialog.show()
        }
    }

    companion object {
        private val TAG = ManageFormsFragment::class.java.simpleName
    }

}