package org.cimsbioko.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.databinding.GenericDetailFragmentBinding
import org.cimsbioko.databinding.GenericDetailFragmentSectionBinding
import org.cimsbioko.navconfig.DetailsSection
import org.cimsbioko.navconfig.ItemDetails
import org.cimsbioko.utilities.*
import java.util.*


class GenericDetailFragment : Fragment() {

    private var detailContainer: LinearLayout? = null
    private var bannerText: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            GenericDetailFragmentBinding.inflate(inflater, container, false).also {
                bannerText = it.genericDetailFragmentBanner
                detailContainer = it.genericDetailFragmentContainer
            }.root

    override fun onDestroyView() {
        super.onDestroyView()
        bannerText = null
        detailContainer = null
    }

    fun showItemDetails(details: ItemDetails, level: String) {
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