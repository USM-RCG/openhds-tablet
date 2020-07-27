package org.cimsbioko.fragment.navigate

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.utilities.LayoutUtils.configureTextWithPayload
import org.cimsbioko.utilities.LayoutUtils.makeTextWithPayload

class DetailToggleFragment : Fragment(), View.OnClickListener {

    private lateinit var layout: RelativeLayout

    private var isEnabled = false
    private var listener: DetailToggleListener? = null

    interface DetailToggleListener {
        fun onDetailToggled()
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        if (ctx is DetailToggleListener) listener = ctx
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return (inflater.inflate(R.layout.detail_toggle_fragment, container, false) as LinearLayout).also { df ->
            layout = makeTextWithPayload(requireActivity(), null, null, null, this, df, 0, null, null, true)
            (layout.layoutParams as LinearLayout.LayoutParams).apply {
                setMargins(0, 0, 0, BUTTON_MARGIN)
            }
        }
    }

    override fun onClick(v: View) {
        listener?.onDetailToggled()
    }

    fun setEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
        if (!isEnabled) {
            layout.visibility = ViewGroup.INVISIBLE
        } else {
            layout.visibility = ViewGroup.VISIBLE
            layout.isClickable = true
            setHighlighted(false)
        }
    }

    fun setHighlighted(isHighlighted: Boolean) {
        requireActivity().also { activity ->
            if (isEnabled && isHighlighted) {
                layout.setBackgroundColor(resources.getColor(R.color.LightGreen, activity.theme))
                configureTextWithPayload(activity, layout, getString(R.string.toggle_fragment_button_show_children),
                        null, null, null, true)
            } else if (isEnabled && !isHighlighted) {
                layout.setBackgroundColor(resources.getColor(R.color.DarkGreen, activity.theme))
                configureTextWithPayload(activity, layout, getString(R.string.toggle_fragment_button_show_details),
                        null, null, null, true)
            }
        }
    }

    companion object {
        private const val BUTTON_MARGIN = 5
    }
}