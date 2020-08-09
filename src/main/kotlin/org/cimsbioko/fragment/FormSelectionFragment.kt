package org.cimsbioko.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.navconfig.forms.Binding
import org.cimsbioko.navconfig.forms.Launcher
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText

class FormSelectionFragment : Fragment() {

    private var listener: FormSelectionListener? = null
    private var formListAdapter: FormSelectionListAdapter? = null

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
        return inflater.inflate(R.layout.form_selection_fragment, container, false)
    }

    fun createFormButtons(values: List<Launcher>) {
        activity?.let { activity ->
            formListAdapter = FormSelectionListAdapter(activity, R.layout.generic_list_item_white_text, values)
            activity.findViewById<ListView>(R.id.form_fragment_listview).apply {
                adapter = formListAdapter
                onItemClickListener = FormClickListener()
                (layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0,
                        if (values.isEmpty()) 0 else resources.getDimensionPixelSize(R.dimen.button_list_divider_height)
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
            return (convertView as? RelativeLayout
                    ?: makeText(requireActivity(), layoutTag = label, background = R.drawable.form_selector)).apply {
                configureText(requireActivity(), primaryText = label)
            }
        }
    }
}