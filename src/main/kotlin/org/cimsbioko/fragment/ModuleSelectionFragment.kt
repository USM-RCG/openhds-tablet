package org.cimsbioko.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.cimsbioko.R
import org.cimsbioko.activity.FieldWorkerActivity
import org.cimsbioko.activity.HierarchyNavigatorActivity
import org.cimsbioko.databinding.ModuleSelectionFragmentBinding
import org.cimsbioko.utilities.ConfigUtils.getActiveModules
import org.cimsbioko.utilities.configureText
import org.cimsbioko.utilities.makeText

class ModuleSelectionFragment : Fragment() {

    private var binding: ModuleSelectionFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ModuleSelectionFragmentBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            val activity = requireActivity()
            val activitiesLayout = binding?.moduleContainer
            getActiveModules(activity).also { modules ->
                val lastIndex = modules.indices.last
                for ((index, module) in modules.withIndex()) {
                    makeText(
                        activity,
                        layoutTag = module.name,
                        listener = { launchNavigator(it) },
                        container = activitiesLayout,
                        background = R.drawable.data_selector
                    ).apply {
                        configureText(
                            activity,
                            text1 = module.launchLabel,
                            text2 = module.launchDescription
                        )
                    }.takeIf { index != lastIndex }
                        ?.root
                        ?.let { it.layoutParams as? LinearLayout.LayoutParams }
                        ?.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.module_button_spacing))
                }
            }
        }
    }

    private fun launchNavigator(view: View) {
        startActivity(Intent(context, HierarchyNavigatorActivity::class.java).apply {
            putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, view.tag as String)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}