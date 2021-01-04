package org.cimsbioko.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.cimsbioko.R
import org.cimsbioko.adapter.FormChecklistAdapter
import org.cimsbioko.databinding.ManageFormsFragmentBinding
import org.cimsbioko.model.FormInstance
import org.cimsbioko.model.LoadedFormInstance
import org.cimsbioko.utilities.FormsHelper.allUnsentFormInstances
import org.cimsbioko.utilities.FormsHelper.deleteFormInstances

class UnsentFormsViewModel : ViewModel() {

    sealed class State {
        object Loading : State()
        data class Loaded(val forms: List<LoadedFormInstance>) : State()
    }

    private val mutableStateFlow = MutableStateFlow<State>(State.Loading)
    val stateFlow = mutableStateFlow.asStateFlow()

    init {
        loadForms()
    }

    private fun loadForms() {
        viewModelScope.launch(Dispatchers.IO) {
            allUnsentFormInstances.map { instances ->
                instances.map { instance -> async(Dispatchers.IO) { instance.load() } }.awaitAll()
            }.onStart {
                mutableStateFlow.value = State.Loading
            }.onEach { loaded ->
                mutableStateFlow.value = State.Loaded(loaded)
            }.collect()
        }
    }

    fun deleteForms(forms: List<FormInstance>) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableStateFlow.value = State.Loading
            deleteFormInstances(forms)
            loadForms()
        }
    }
}


class ManageFormsFragment : Fragment() {

    private val model: UnsentFormsViewModel by viewModels()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = FormChecklistAdapter(requireContext(), R.id.form_instance_check_item_orange, ArrayList())
        savedInstanceState?.also { adapter?.restoreCheckStates(it) }
        lifecycleScope.launchWhenStarted {
            model.stateFlow.collect { state ->
                isLoading = when (state) {
                    UnsentFormsViewModel.State.Loading -> true
                    is UnsentFormsViewModel.State.Loaded -> {
                        adapter?.clear()
                        adapter?.addAll(state.forms)
                        false
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter?.saveCheckStates(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ManageFormsFragmentBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.also {
            progressBar = it.progressBar
            listView = it.manageFormsFragmentListview
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        progressBar = null
        listView = null
        deleteButton = null
        deleteConfirmDialog = null
    }

    private fun deleteSelected() {
        adapter?.also { adapter ->
            adapter.checkedInstances.also { selected ->
                adapter.removeAll(selected)
                model.deleteForms(selected)
            }
        }
    }

    private inner class ButtonListener : View.OnClickListener {
        override fun onClick(v: View) {
            if (adapter?.checkedInstances?.isNotEmpty() == true) deleteConfirmDialog?.show()
        }
    }
}