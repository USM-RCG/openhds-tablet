package org.cimsbioko.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.databinding.FormSelectionFragmentBinding
import org.cimsbioko.databinding.GenericListItemBinding
import org.cimsbioko.navconfig.Binding
import org.cimsbioko.navconfig.Launcher
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText

class FormSelectionFragment : Fragment() {

    private var listener: FormSelectionListener? = null
    private var formListAdapter: FormSelectionListAdapter? = null
    private var listView: ListView? = null

    interface FormSelectionListener {
        fun onFormSelected(binding: Binding)
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        if (ctx is FormSelectionListener) {
            listener = ctx
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FormSelectionFragmentBinding.inflate(inflater, container, false).also {
            listView = it.formFragmentListview
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listView = null
        formListAdapter = null
    }

    fun createFormButtons(values: List<Launcher>) {
        activity?.let { activity ->
            listView?.apply {
                formListAdapter = FormSelectionListAdapter(activity, R.layout.generic_list_item, values)
                adapter = formListAdapter
                onItemClickListener = FormClickListener()
                (layoutParams as? LinearLayout.LayoutParams)?.setMargins(0, 0, 0,
                        if (values.isEmpty()) 0
                        else resources.getDimensionPixelSize(R.dimen.button_list_divider_height)
                )
            }
        }
    }

    private inner class FormClickListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            listener?.let { listener ->
                formListAdapter?.let { adapter ->
                    adapter.getItem(position)?.let { item ->
                        listener.onFormSelected(item.binding)
                    }
                }
            }
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