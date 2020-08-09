package org.cimsbioko.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.data.DataWrapper
import org.cimsbioko.provider.DatabaseAdapter
import org.cimsbioko.utilities.MessageUtils.showShortToast
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText

class DataSelectionFragment : Fragment() {

    private lateinit var listView: ListView
    private var listener: DataSelectionListener? = null
    private var dataWrapperAdapter: DataSelectionListAdapter? = null

    interface DataSelectionListener {
        fun onDataSelected(data: DataWrapper)
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        if (ctx is DataSelectionListener) {
            listener = ctx
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return (inflater.inflate(R.layout.data_selection_fragment, container, false) as ViewGroup).also { viewGroup: ViewGroup ->
            listView = viewGroup.findViewById<ListView>(R.id.data_fragment_listview).also { listView ->
                listView.onItemClickListener = DataClickListener()
                registerForContextMenu(listView)
            }
        }
    }

    fun populateData(data: List<DataWrapper>) {
        dataWrapperAdapter = DataSelectionListAdapter(requireContext(), R.layout.generic_list_item_white_text, data)
        listView.adapter = dataWrapperAdapter
    }

    fun clearData() {
        dataWrapperAdapter?.clear()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        if (v.id == R.id.data_fragment_listview) {
            requireActivity().menuInflater.inflate(R.menu.data_selection_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save_favorite) {
            getItem((item.menuInfo as AdapterContextMenuInfo).position)?.let { selected ->
                DatabaseAdapter.addFavorite(selected)
                showShortToast(requireContext(), R.string.saved_favorite)
            }
            return true
        }
        return super.onContextItemSelected(item)
    }

    private inner class DataClickListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            getItem(position)?.also { listener?.onDataSelected(it) }
        }
    }

    private inner class DataSelectionListAdapter(context: Context, resource: Int, objects: List<DataWrapper>) : ArrayAdapter<DataWrapper>(context, resource, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getItem(position).let { item ->
                (convertView as? RelativeLayout
                        ?: makeText(requireActivity(), layoutTag = item?.name, background = R.drawable.data_selector)).apply {
                    configureText(requireActivity(),
                            primaryText = item?.name,
                            secondaryText = item?.extId,
                            stringsPayload = item?.stringsPayload,
                            stringsIdsPayload = item?.stringIdsPayload,
                            centerText = false)
                }
            }
        }
    }

    private fun getItem(position: Int): DataWrapper? {
        return dataWrapperAdapter?.getItem(position)
    }
}