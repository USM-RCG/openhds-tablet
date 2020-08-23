package org.cimsbioko.fragment.navigate

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
import org.cimsbioko.utilities.configureTextWithLabel
import org.cimsbioko.utilities.isBlank
import org.cimsbioko.utilities.makeLargeTextWithLabel
import java.util.*

private const val LABEL_COLOR = R.color.DarkGray
private const val VALUE_COLOR = R.color.White

class GenericDetailFragment : Fragment() {

    private lateinit var detailContainer: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            (inflater.inflate(R.layout.generic_detail_fragment, container, false) as ScrollView)
                    .also { detailContainer = it.findViewById(R.id.generic_detail_fragment_container) }

    fun showItemDetails(details: ItemDetails) {
        details.banner?.let { setBannerText(it) }
        details.sections?.let { rebuildSections(it) }
    }

    private fun rebuildSections(sections: List<DetailsSection>) {
        detailContainer.apply { findViewsByTag("detail_section").forEach { removeView(it) } }
        sections.forEach { section ->
            (activity
                    ?.layoutInflater
                    ?.inflate(R.layout.generic_detail_fragment_section, detailContainer, false) as? LinearLayout)
                    ?.let { sectionLayout ->
                        detailContainer.addView(sectionLayout)
                        section.banner?.let { sectionLayout.findViewById<TextView>(R.id.generic_detail_fragment_section_banner).text = it }
                        section.details?.forEach { if (!it.value.isBlank) sectionLayout.addTextView(it.key, it.value) }
                    }
        }
    }

    private fun setBannerText(text: String) {
        detailContainer.findViewById<TextView>(R.id.generic_detail_fragment_banner).text = text
    }

    private fun LinearLayout.addTextView(label: String, value: String?) {
        activity?.let {
            addView(makeLargeTextWithLabel(it).apply {
                configureTextWithLabel(label, value, LABEL_COLOR, VALUE_COLOR)
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