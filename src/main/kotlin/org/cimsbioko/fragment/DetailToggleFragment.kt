package org.cimsbioko.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.cimsbioko.R
import org.cimsbioko.databinding.DetailToggleFragmentBinding
import org.cimsbioko.databinding.GenericListItemBinding
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText
import org.cimsbioko.viewmodel.NavModel

class DetailToggleFragment : Fragment(), View.OnClickListener {

    private val model: NavModel by activityViewModels()

    private var layout: View? = null
    private var buttonLayout: GenericListItemBinding? = null

    private var isEnabled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return DetailToggleFragmentBinding.inflate(inflater, container, false)
                .root
                .also {
                    layout = it
                    buttonLayout = makeText(requireActivity(), listener = this, container = it)
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model.detailsToggleShown.onEach { setEnabled(it) }.launchIn(lifecycleScope)
        model.itemDetailsShown.onEach { setDetailsShown(it) }.launchIn(lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        layout = null
        buttonLayout = null
    }

    override fun onClick(v: View) = model.toggleDetail()

    private fun setEnabled(isEnabled: Boolean) {
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

    private fun setDetailsShown(detailsShown: Boolean) {
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