package org.cimsbioko.fragment.navigate

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.data.DataWrapper
import org.cimsbioko.navconfig.HierarchyPath
import org.cimsbioko.navconfig.NavigatorConfig
import org.cimsbioko.navconfig.NavigatorConfig.Companion.instance
import org.cimsbioko.utilities.LayoutUtils.configureTextWithPayload
import org.cimsbioko.utilities.LayoutUtils.makeTextWithPayload

class HierarchyButtonFragment : Fragment(), View.OnClickListener {

    private lateinit var scrollView: ScrollView
    private lateinit var levelViews: Map<String, RelativeLayout>
    private lateinit var config: NavigatorConfig

    private var listener: HierarchyButtonListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = instance
    }

    interface HierarchyButtonListener {
        fun onHierarchyButtonClicked(level: String)
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        if (ctx is HierarchyButtonListener) listener = ctx
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.hierarchy_button_fragment, container, false).apply {
            scrollView = findViewById(R.id.hierbutton_scroll)
            levelViews = findViewById<ViewGroup>(R.id.hierbutton_layout).let { buttonLayout ->
                val activity = requireActivity()
                config.levels.map { level ->
                    level to makeTextWithPayload(
                            activity, config.getLevelLabel(level), null, level, this@HierarchyButtonFragment,
                            buttonLayout, R.drawable.data_selector, null, null, true).apply {
                        visibility = View.GONE
                        (layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0, BUTTON_MARGIN)
                    }
                }.toMap()
            }
        }
    }

    fun update(path: HierarchyPath) {

        // Configure and show all hierarchy buttons for levels in path
        for (lvl in path.levels) {
            updateButton(lvl, path[lvl])
            setVisible(lvl, true)
        }

        // Hide all buttons not in path
        for (i in path.depth() until config.levels.size) {
            setVisible(config.levels[i], false)
        }

        // If we can go deeper, show the next level as disabled with the level name
        if (path.depth() < config.levels.size) {
            config.levels[path.depth()].let { nextLevel ->
                updateButton(nextLevel, path[nextLevel])
                setVisible(nextLevel, true)
            }
        }

        // Scroll to the bottom when the buttons overflow
        scrollView.apply { post { if (canScrollVertically(1)) fullScroll(View.FOCUS_DOWN) } }
    }

    private fun updateButton(level: String, data: DataWrapper?) {
        if (data == null) {
            setButtonLabel(level, config.getLevelLabel(level), null, true)
            setHighlighted(level, true)
        } else {
            setButtonLabel(level, data.name, data.extId, false)
            setHighlighted(level, false)
        }
    }

    private fun setVisible(level: String, visible: Boolean) {
        levelViews[level]?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun setHighlighted(level: String, highlighted: Boolean) {
        levelViews[level]?.let { layout ->
            // Defer setting pressed state so it isn't overwritten when run within a click handler
            Handler(Looper.getMainLooper()).postDelayed({
                layout.isClickable = !highlighted
                layout.isPressed = highlighted
            }, 100)
        }
    }

    private fun setButtonLabel(level: String, name: String?, id: String?, centerText: Boolean) {
        levelViews[level]?.let { layout ->
            configureTextWithPayload(requireActivity(), layout, name, id, null, null, centerText)
        }
    }

    override fun onClick(v: View) {
        listener?.onHierarchyButtonClicked(v.tag as String)
    }

    companion object {
        private const val BUTTON_MARGIN = 5 // margin in layout XML is ignored for some reason
    }
}