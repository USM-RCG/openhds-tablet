package org.cimsbioko.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.databinding.DetailToggleFragmentBinding
import org.cimsbioko.databinding.GenericListItemBinding
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText

class DetailToggleFragment : Fragment(), View.OnClickListener {

    private var layout: View? = null
    private var buttonLayout: GenericListItemBinding? = null

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
        return DetailToggleFragmentBinding.inflate(inflater, container, false)
                .root
                .also {
                    layout = it
                    buttonLayout = makeText(requireActivity(), listener = this, container = it)
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        layout = null
        buttonLayout = null
    }

    override fun onClick(v: View) {
        listener?.onDetailToggled()
    }

    fun setEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
        if (!isEnabled) {
            layout?.visibility = ViewGroup.GONE
        } else {
            buttonLayout?.root?.apply {
                background = ContextCompat.getDrawable(requireContext(), R.drawable.detail_toggle)
                isClickable = true
            }
            setDetailsShown(false)
            layout?.visibility = ViewGroup.VISIBLE
        }
    }

    fun setDetailsShown(detailsShown: Boolean) {
        requireActivity().also { activity ->
            buttonLayout?.apply {
                if (isEnabled && detailsShown) {
                    configureText(activity, getString(R.string.toggle_fragment_button_hide_details))
                } else if (isEnabled && !detailsShown) {
                    configureText(activity, getString(R.string.toggle_fragment_button_show_details))
                }
            }
        }
    }
}