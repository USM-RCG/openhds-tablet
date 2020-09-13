package org.cimsbioko.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import org.cimsbioko.R
import org.cimsbioko.activity.FieldWorkerActivity
import org.cimsbioko.activity.HierarchyNavigatorActivity
import org.cimsbioko.databinding.FavoritesFragmentBinding
import org.cimsbioko.databinding.GenericListItemBinding
import org.cimsbioko.model.HierarchyItem
import org.cimsbioko.navconfig.DefaultQueryHelper.getByHierarchyId
import org.cimsbioko.navconfig.HierarchyPath.Companion.fromString
import org.cimsbioko.navconfig.NavigatorConfig
import org.cimsbioko.provider.DatabaseAdapter
import org.cimsbioko.utilities.ConfigUtils.getActiveModules
import org.cimsbioko.utilities.MessageUtils.showShortToast
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText
import org.cimsbioko.utilities.toLevelIcon
import kotlin.coroutines.CoroutineContext

class FavoritesFragment : Fragment(), CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var list: ListView? = null
    private var progress: ProgressBar? = null
    private var dataAdapter: FavoriteAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FavoritesFragmentBinding.inflate(inflater, container, false).apply {
            dataAdapter = FavoriteAdapter(requireActivity())
            list = favoritesList.apply {
                adapter = dataAdapter
                onItemClickListener = ClickListener()
                registerForContextMenu(this)
            }
            progress = progressBar
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        list?.let { unregisterForContextMenu(it) }
        list = null
        progress = null
        dataAdapter = null
    }

    override fun onResume() {
        job = Job()
        loadData()
        super.onResume()
    }

    override fun onPause() {
        job.cancel("fragment paused")
        super.onPause()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        if (v.id == R.id.favorites_list) {
            requireActivity().menuInflater.inflate(R.menu.favorite_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        when (item.itemId) {
            R.id.find_favorite -> {
                getItem(info.position)?.let { selectFavorite(it) }
                return true
            }
            R.id.forget_favorite -> {
                getItem(info.position)?.let {
                    DatabaseAdapter.removeFavorite(it.hierarchyId)
                    showShortToast(requireContext(), R.string.removed_favorite)
                    loadData()
                }
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun getItem(position: Int): HierarchyItem? {
        return list?.getItemAtPosition(position) as? HierarchyItem
    }

    private fun selectFavorite(selected: HierarchyItem) {
        val ctx = requireContext()
        fromString(selected.hierarchyId)?.let { path ->
            getActiveModules(ctx).firstOrNull()?.also { firstModule ->
                startActivity(Intent(ctx, HierarchyNavigatorActivity::class.java).apply {
                    putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, firstModule.name)
                    putExtra(HierarchyNavigatorActivity.HIERARCHY_PATH_KEY, path)
                })
            }
        } ?: showShortToast(ctx, R.string.no_active_modules)
    }

    private fun showLoading(loading: Boolean) {
        progress?.visibility = if (loading) View.VISIBLE else View.GONE
        list?.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private inner class ClickListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            getItem(position)?.let { selectFavorite(it) }
        }
    }

    private inner class FavoriteAdapter(
            context: Context
    ) : ArrayAdapter<HierarchyItem>(context, R.layout.generic_list_item) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return requireActivity().let { activity ->
                (convertView?.let { GenericListItemBinding.bind(it) }
                        ?: makeText(activity, background = R.drawable.data_selector)).also { binding ->
                    getItem(position)?.apply {
                        NavigatorConfig.instance.modules
                                .firstOrNull()
                                ?.getHierFormatter(level)
                                ?.formatItem(this)
                                ?.also {
                                    binding.configureText(activity,
                                            text1 = it.heading,
                                            text2 = it.subheading,
                                            details = it.details,
                                            centerText = false,
                                            iconRes = level.toLevelIcon(),
                                            detailsPadding = resources.getDimensionPixelSize(R.dimen.detail_padding)
                                    )
                                }
                    }
                }.root
            }
        }
    }

    private fun loadData() = launch {

        showLoading(true)
        dataAdapter?.clear()

        val items = withContext(Dispatchers.IO) {
            DatabaseAdapter.let { db ->
                db.favoriteIds
                        .map { it to getByHierarchyId(it)?.first }
                        .partition { (_, item) -> item != null }
                        .let { (found, lost) ->
                            lost.forEach { (id, _) -> db.removeFavorite(id) }
                            found.mapNotNull { (_, item) -> item }
                        }
            }
        }

        dataAdapter?.addAll(items)
        showLoading(false)
    }

}