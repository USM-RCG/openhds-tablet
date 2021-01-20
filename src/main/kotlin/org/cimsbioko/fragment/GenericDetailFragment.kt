package org.cimsbioko.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.cimsbioko.R
import org.cimsbioko.databinding.GenericDetailFragmentBinding
import org.cimsbioko.databinding.GenericDetailFragmentSectionBinding
import org.cimsbioko.navconfig.DetailsSection
import org.cimsbioko.navconfig.ItemDetails
import org.cimsbioko.utilities.*
import org.cimsbioko.viewmodel.NavModel
import java.util.*


class GenericDetailFragment : Fragment() {

    private val model: NavModel by activityViewModels()

    private var progressBar: ProgressBar? = null
    private var scrollView: ScrollView? = null
    private var detailContainer: LinearLayout? = null
    private var bannerText: TextView? = null

    private var isLoading: Boolean
        get() = progressBar?.isVisible ?: false
        set(loading) {
            progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
            scrollView?.visibility = if (loading) View.GONE else View.VISIBLE
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            model.details.asStateFlow().collectLatest { state ->
                isLoading = when (state) {
                    NavModel.Details.Loading -> true
                    is NavModel.Details.Loaded -> {
                        showItemDetails(state.details, model.level)
                        false
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        GenericDetailFragmentBinding.inflate(inflater, container, false).also {
            progressBar = it.progressBar
            scrollView = it.scrollView
            bannerText = it.genericDetailFragmentBanner
            detailContainer = it.genericDetailFragmentContainer
        }.root

    override fun onDestroyView() {
        super.onDestroyView()
        progressBar = null
        scrollView = null
        bannerText = null
        detailContainer = null
    }

    private fun showItemDetails(details: ItemDetails, level: String) {
        details.banner?.let { setBannerText(it, level) }
        details.sections?.let { rebuildSections(it) }
    }

    private fun rebuildSections(sections: List<DetailsSection>) {
        detailContainer?.apply { findViewsByTag("detail_section").forEach { removeView(it) } }
        sections
                .map { section -> section to section.details?.filter { !it.value.isBlank } }
                .filter { (_, details) -> details?.isNotEmpty() == true }
                .forEach { (section, details) ->
                    activity?.layoutInflater
                            ?.let { GenericDetailFragmentSectionBinding.inflate(it, detailContainer, false) }
                            ?.also { sectionBinding ->
                                detailContainer?.addView(sectionBinding.root)
                                section.banner?.let {
                                    sectionBinding.genericDetailFragmentSectionBanner.text = it
                                }
                                details?.forEach {
                                    sectionBinding.genericDetailFragmentSectionDetails.addTextView(it.key, it.value)
                                }
                            }
                }
    }

    private fun setBannerText(text: String, level: String) {
        bannerText?.apply { setTextWithIcon(text, level.toLevelIcon(), ContextCompat.getColor(context, R.color.Black)) }
    }

    private fun LinearLayout.addTextView(label: String, value: String?) {
        activity?.let {
            addView(makeTextWithLabel(it).apply { configureTextWithLabel(label, value) }.root)
        }
    }
}

private fun ViewGroup.findViewsByTag(tag: String): List<View> {
    val views = ArrayList<View>()
    val childCount = childCount
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is ViewGroup) views.addAll(child.findViewsByTag(tag))
        child.tag?.takeIf { it == tag }?.also { views.add(child) }
    }
    return views
}