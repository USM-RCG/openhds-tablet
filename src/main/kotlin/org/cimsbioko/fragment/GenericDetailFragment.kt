package org.cimsbioko.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.cimsbioko.R
import org.cimsbioko.navconfig.DetailsSection
import org.cimsbioko.navconfig.ItemDetails
import org.cimsbioko.utilities.*
import java.util.*


class GenericDetailFragment : Fragment() {

    private lateinit var detailContainer: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            (inflater.inflate(R.layout.generic_detail_fragment, container, false) as ScrollView)
                    .also { detailContainer = it.findViewById(R.id.generic_detail_fragment_container) }

    fun showItemDetails(details: ItemDetails, level: String) {
        details.banner?.let { setBannerText(it, level) }
        details.sections?.let { rebuildSections(it) }
    }

    private fun rebuildSections(sections: List<DetailsSection>) {
        detailContainer.apply { findViewsByTag("detail_section").forEach { removeView(it) } }
        sections
                .map { section -> section to section.details?.filter { !it.value.isBlank } }
                .filter { (_, details) -> details?.isNotEmpty() == true }
                .forEach { (section, details) ->
                    (activity
                            ?.layoutInflater
                            ?.inflate(R.layout.generic_detail_fragment_section, detailContainer, false) as? LinearLayout)
                            ?.let { sectionLayout ->
                                detailContainer.addView(sectionLayout)
                                section.banner?.let {
                                    sectionLayout.findViewById<TextView>(R.id.generic_detail_fragment_section_banner).text = it
                                }
                                sectionLayout.findViewById<LinearLayout>(R.id.generic_detail_fragment_section_details)
                                        .let { detailLayout ->
                                            details?.forEach {
                                                detailLayout.addTextView(it.key, it.value)
                                            }
                                        }
                            }
                }
    }

    private fun setBannerText(text: String, level: String) {
        detailContainer.findViewById<TextView>(R.id.generic_detail_fragment_banner)
                .setTextWithIcon(text, level.toLevelIcon(), resources.getColor(R.color.Black))
    }

    private fun LinearLayout.addTextView(label: String, value: String?) {
        activity?.let {
            addView(makeTextWithLabel(it).apply {
                configureTextWithLabel(label, value)
            })
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