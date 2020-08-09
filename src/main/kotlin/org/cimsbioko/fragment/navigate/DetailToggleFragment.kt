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
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText

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
            layout = makeText(requireActivity(), listener = this, container = df).apply {
                (layoutParams as LinearLayout.LayoutParams).apply { setMargins(0, 0, 0, BUTTON_MARGIN) }
            }
        }
    }

    override fun onClick(v: View) {
        listener?.onDetailToggled()
    }

    fun setEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
        with(layout) {
            if (!isEnabled) {
                visibility = ViewGroup.INVISIBLE
            } else {
                visibility = ViewGroup.VISIBLE
                isClickable = true
                setHighlighted(false)
            }
        }
    }

    fun setHighlighted(isHighlighted: Boolean) {
        requireActivity().also { activity ->
            with(layout) {
                if (isEnabled && isHighlighted) {
                    setBackgroundColor(resources.getColor(R.color.LightGreen, activity.theme))
                    configureText(activity, getString(R.string.toggle_fragment_button_show_children))
                } else if (isEnabled && !isHighlighted) {
                    setBackgroundColor(resources.getColor(R.color.DarkGreen, activity.theme))
                    configureText(activity, getString(R.string.toggle_fragment_button_show_details))
                }
            }
        }
    }

    companion object {
        private const val BUTTON_MARGIN = 5
    }
}