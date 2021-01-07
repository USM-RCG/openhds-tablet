package org.cimsbioko.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.cimsbioko.R
import org.cimsbioko.databinding.FormSelectionFragmentBinding
import org.cimsbioko.databinding.GenericListItemBinding
import org.cimsbioko.navconfig.Binding
import org.cimsbioko.navconfig.Launcher
import org.cimsbioko.utilities.MessageUtils
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText
import org.cimsbioko.viewmodel.NavModel

class FormSelectionFragment : Fragment() {

    companion object {
        const val LAUNCH_FORM_REQUEST_CODE: Int = 1
    }

    private val model: NavModel by activityViewModels()

    private var progressBar: ProgressBar? = null
    private var formListAdapter: FormSelectionListAdapter? = null
    private var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            model.launcherItems.asStateFlow().collectLatest { state ->
                isLoading = when (state) {
                    NavModel.LauncherItems.Loading -> true
                    is NavModel.LauncherItems.Loaded -> {
                        createFormButtons(state.items)
                        false
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FormSelectionFragmentBinding.inflate(inflater, container, false).also {
            listView = it.formFragmentListview
            progressBar = it.progressBar
        }.root
    }

    private var isLoading: Boolean
        get() = progressBar?.isVisible == true
        set(loading) {
            progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
            listView?.visibility = if (loading) View.GONE else View.VISIBLE
        }

    override fun onDestroyView() {
        super.onDestroyView()
        listView = null
        formListAdapter = null
        progressBar = null
    }

    private fun createFormButtons(values: List<Launcher>) {
        activity?.let { activity ->
            listView?.apply {
                formListAdapter = FormSelectionListAdapter(activity, R.layout.generic_list_item, values)
                adapter = formListAdapter
                onItemClickListener = FormClickListener()
            }
        }
    }

    fun launchNewForm(binding: Binding) {
        lifecycleScope.launch {
            try {
                MessageUtils.showShortToast(requireContext(), R.string.launching_form)
                model.generateFormInstance(binding).also { startActivityForResult(it, LAUNCH_FORM_REQUEST_CODE) }
            } catch (e: Exception) {
                MessageUtils.showShortToast(requireContext(), "failed to launch form: " + e.message)
            }
        }
    }


    private inner class FormClickListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            formListAdapter?.let { adapter ->
                adapter.getItem(position)?.let { launchNewForm(it.binding) }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == LAUNCH_FORM_REQUEST_CODE) {
            data?.data?.also { model.processNewFormResult(it) }
        }
    }

    private inner class FormSelectionListAdapter(
        context: Context, resource: Int, objects: List<Launcher>
    ) : ArrayAdapter<Launcher?>(context, resource, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val label = formListAdapter?.getItem(position)?.label
            return (convertView?.let { GenericListItemBinding.bind(it) }
                ?: makeText(requireActivity(), layoutTag = label, background = R.drawable.form_selector)).apply {
                configureText(requireActivity(), text1 = label)
            }.root
        }
    }
}