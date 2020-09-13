package org.cimsbioko.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText

class DetailToggleFragment : Fragment(), View.OnClickListener {

    private lateinit var layout: View
    private lateinit var buttonLayout: RelativeLayout

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
        return inflater.inflate(R.layout.detail_toggle_fragment, container, false)
                .let { it as LinearLayout }
                .also {
                    layout = it
                    buttonLayout = makeText(requireActivity(), listener = this, container = it)
                }
    }

    override fun onClick(v: View) {
        listener?.onDetailToggled()
    }

    fun setEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
        if (!isEnabled) {
            layout.visibility = ViewGroup.GONE
        } else {
            with (buttonLayout) {
                background = ContextCompat.getDrawable(requireContext(), R.drawable.detail_toggle)
                isClickable = true
            }
            setDetailsShown(false)
            layout.visibility = ViewGroup.VISIBLE
        }
    }

    fun setDetailsShown(detailsShown: Boolean) {
        requireActivity().also { activity ->
            with(buttonLayout) {
                if (isEnabled && detailsShown) {
                    configureText(activity, getString(R.string.toggle_fragment_button_hide_details))
                } else if (isEnabled && !detailsShown) {
                    configureText(activity, getString(R.string.toggle_fragment_button_show_details))
                }
            }
        }
    }
}