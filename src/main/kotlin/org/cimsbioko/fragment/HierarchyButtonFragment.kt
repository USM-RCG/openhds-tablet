package org.cimsbioko.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.widget.TextViewCompat.setTextAppearance
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.cimsbioko.R
import org.cimsbioko.data.DataWrapper
import org.cimsbioko.databinding.GenericListItemBinding
import org.cimsbioko.databinding.HierarchyButtonFragmentBinding
import org.cimsbioko.navconfig.HierarchyPath
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText
import org.cimsbioko.utilities.toLevelIcon
import org.cimsbioko.viewmodel.NavModel

class HierarchyButtonFragment : Fragment(), View.OnClickListener {

    private val model: NavModel by activityViewModels()

    private var scrollView: ScrollView? = null
    private var levelViews: Map<String, GenericListItemBinding>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return HierarchyButtonFragmentBinding.inflate(inflater, container, false).also { binding ->
            scrollView = binding.hierbuttonScroll
            levelViews = binding.hierbuttonLayout.let { buttonLayout ->
                val activity = requireActivity()
                model.config.levels.map { level ->
                    level to makeText(
                            activity,
                            layoutTag = level,
                            listener = this@HierarchyButtonFragment,
                            container = buttonLayout,
                            background = R.drawable.data_selector).apply {
                        configureText(activity, text1 = model.config.getLevelLabel(level))
                        root.visibility = View.GONE
                    }
                }.toMap()
            }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch { model.hierarchyPath.collectLatest { path -> update(path) } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scrollView = null
        levelViews = null
    }

    fun update(path: HierarchyPath) {

        // Configure and show all hierarchy buttons for levels in path
        for (lvl in path.levels) {
            updateButton(lvl, path[lvl])
            setVisible(lvl, true)
        }

        // Hide all buttons not in path
        for (i in path.depth() until model.config.levels.size) {
            setVisible(model.config.levels[i], false)
        }

        // If we can go deeper, show the next level as disabled with the level name
        if (path.depth() < model.config.levels.size) {
            model.config.levels[path.depth()].let { nextLevel ->
                updateButton(nextLevel, path[nextLevel])
                setVisible(nextLevel, true, isLast = true)
            }
        } else setVisible(model.config.levels.last(), true, isLast = true)

        // Scroll to the bottom when the buttons overflow
        scrollView?.apply { post { if (canScrollVertically(1)) fullScroll(View.FOCUS_DOWN) } }
    }

    private fun updateButton(level: String, data: DataWrapper?) {
        if (data == null) {
            setButtonLabel(level, model.config.getLevelLabel(level), center = true)
            setHighlighted(level, true)
        } else {
            setButtonLabel(level, data.name, data.extId, center = false, showIcon = true)
            setHighlighted(level, false)
        }
    }

    private fun setVisible(level: String, visible: Boolean, isLast: Boolean = false) {
        levelViews?.get(level)?.root?.apply {
            visibility = if (visible) View.VISIBLE else View.GONE
            (layoutParams as? LinearLayout.LayoutParams)?.setMargins(0, 0, 0, if (isLast) 0 else resources.getDimensionPixelSize(R.dimen.hier_button_spacing))
        }
    }

    private fun setHighlighted(level: String, highlighted: Boolean) {
        levelViews?.get(level)?.apply {
            root.apply {
                isClickable = !highlighted // block repeated clicks
                setBackgroundResource(if (highlighted) R.drawable.form_list_header else R.drawable.data_selector)
            }
            setTextAppearance(
                    primaryText,
                    if (highlighted) R.style.TextAppearance_AppCompat_Large
                    else R.style.TextAppearance_AppCompat_Large_Inverse
            )
        }
    }

    private fun setButtonLabel(level: String, name: String?, id: String? = null, center: Boolean, showIcon: Boolean = false) {
        levelViews?.get(level)?.configureText(
                requireActivity(),
                text1 = name,
                text2 = id,
                centerText = center,
                iconRes = if (showIcon) level.toLevelIcon() else null
        )
    }

    override fun onClick(v: View) = model.jumpUp(v.tag as String)
}