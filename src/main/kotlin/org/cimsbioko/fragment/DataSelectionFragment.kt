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
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.cimsbioko.R
import org.cimsbioko.databinding.DataSelectionFragmentBinding
import org.cimsbioko.databinding.GenericListItemBinding
import org.cimsbioko.model.HierarchyItem
import org.cimsbioko.navconfig.HierFormatter
import org.cimsbioko.provider.DatabaseAdapter
import org.cimsbioko.utilities.MessageUtils.showShortToast
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText
import org.cimsbioko.utilities.toLevelIcon
import org.cimsbioko.viewmodel.NavModel

class DataSelectionFragment : Fragment() {

    private val model: NavModel by activityViewModels()

    private var progressBar: ProgressBar? = null
    private var listView: ListView? = null
    private var adapter: DataSelectionListAdapter? = null
    private var itemFormatter: HierFormatter? = null

    private var isLoading: Boolean
        get() = progressBar?.isVisible == true
        set(loading) {
            progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
            listView?.visibility = if (loading) View.GONE else View.VISIBLE
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return DataSelectionFragmentBinding.inflate(inflater, container, false).also {
            listView = it.dataFragmentListview.also { listView ->
                listView.onItemClickListener = DataClickListener()
                registerForContextMenu(listView)
            }
            progressBar = it.progressBar
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            model.childItems.asStateFlow().collectLatest { result ->
                isLoading = when (result) {
                    NavModel.ChildItems.Loading -> true
                    is NavModel.ChildItems.Loaded -> {
                        model.childItemFormatter?.also { populateData(result.items, it) }
                        false
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listView?.let { unregisterForContextMenu(it) }
        listView = null
        progressBar = null
    }

    private fun populateData(data: List<HierarchyItem>, formatter: HierFormatter) {
        itemFormatter = formatter
        adapter = DataSelectionListAdapter(requireContext(), R.layout.generic_list_item, data)
        listView?.adapter = adapter
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        if (v.id == R.id.data_fragment_listview) {
            requireActivity().menuInflater.inflate(R.menu.data_selection_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save_favorite) {
            getItem((item.menuInfo as AdapterContextMenuInfo).position)?.let { selected ->
                DatabaseAdapter.addFavorite(selected.wrapped)
                showShortToast(requireContext(), R.string.saved_favorite)
            }
            return true
        }
        return super.onContextItemSelected(item)
    }

    private inner class DataClickListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            lifecycleScope.launch { getItem(position)?.also { model.stepDown(it.wrapped) } }
        }
    }

    private inner class DataSelectionListAdapter(
            context: Context,
            resource: Int,
            objects: List<HierarchyItem>
    ) : ArrayAdapter<HierarchyItem>(context, resource, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getItem(position).let { item ->
                val formatted = item?.let { itemFormatter?.formatItem(item) }
                (convertView?.let { GenericListItemBinding.bind(it) }
                        ?: makeText(requireActivity(), layoutTag = formatted?.heading, background = R.drawable.data_selector)).apply {
                    configureText(requireActivity(),
                            text1 = formatted?.heading,
                            text2 = formatted?.subheading,
                            details = formatted?.details,
                            centerText = false,
                            iconRes = item?.level?.toLevelIcon(),
                            detailsPadding = resources.getDimensionPixelSize(R.dimen.detail_padding)
                    )
                }
            }.root
        }
    }

    private fun getItem(position: Int): HierarchyItem? {
        return adapter?.getItem(position)
    }
}