package org.cimsbioko.utilities

import android.app.Activity
import android.text.SpannableString
import android.text.style.DynamicDrawableSpan.ALIGN_BASELINE
import android.text.style.ImageSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import org.cimsbioko.R
import org.cimsbioko.navconfig.Hierarchy


// Create a new Layout that contains two text views and optionally several key-value text views beneath.
fun makeText(activity: Activity, layoutTag: Any? = null, listener: View.OnClickListener? = null,
             container: ViewGroup? = null, background: Int = 0): RelativeLayout =
        activity.layoutInflater.inflate(R.layout.generic_list_item, null)
                .let { it as RelativeLayout }
                .apply {
                    tag = layoutTag
                    listener?.also { setOnClickListener(it) }
                    container?.addView(this)
                    background.takeIf { it != 0 }?.also { setBackgroundResource(it) }
                }

// Pass new data to a layout that was created with makeTextWithPayload().
fun RelativeLayout.configureText(activity: Activity, primaryText: String? = null,
                                 secondaryText: String? = null, details: Map<String, String?>? = null,
                                 centerText: Boolean = true, iconRes: Int? = null, detailsPadding: Int = 0) {

    fun TextView.configure(s: String?, iconRes: Int? = null) {
        s?.let { str -> setTextWithIcon(str, iconRes) }
        visibility = if (s == null) View.GONE else View.VISIBLE
        gravity = Gravity.CENTER
    }

    findViewById<TextView>(R.id.primary_text).configure(primaryText, iconRes)
    findViewById<TextView>(R.id.secondary_text).configure(secondaryText)
    findViewById<LinearLayout>(R.id.details_container).apply {
        removeAllViews()
        details?.also { sp ->
            for ((key, value) in sp) {
                if (!value.isBlank) {
                    addView(makeTextWithLabel(activity).apply {
                        configureTextWithLabel(labelText = key, valueText = value)
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    })
                }
            }
        }
        visibility = if (childCount == 0) View.GONE else View.VISIBLE
        gravity = if (centerText) Gravity.CENTER else Gravity.NO_GRAVITY
        detailsPadding.takeIf { it > 0 }?.also { setPadding(it, it, it, it) }
    }
}

// Create a pair of text views to represent some value plus its label, with given colors.
fun makeTextWithLabel(activity: Activity): RelativeLayout =
        activity.layoutInflater.inflate(R.layout.value_with_label, null)
                .let { it as RelativeLayout }

val CharSequence?.isBlank: Boolean
    get() = this == null || isEmpty() || this == "null"

fun RelativeLayout.configureTextWithLabel(labelText: String, valueText: String?,
                                          labelColorId: Int = R.color.Black, valueColorId: Int = R.color.Black) {
    val resources = context.resources
    findViewById<TextView>(R.id.label_text)?.apply {
        text = labelText
        setTextColor(resources.getColor(labelColorId))
    }
    findViewById<TextView>(R.id.value_text)?.apply {
        text = valueText
        setTextColor(resources.getColor(valueColorId))
    }
}

fun String?.toLevelIcon(): Int? {
    return this?.let {
        when (it) {
            Hierarchy.HOUSEHOLD -> R.drawable.location_logo
            Hierarchy.INDIVIDUAL -> R.drawable.individual_logo
            else -> R.drawable.hierarchy_logo
        }
    }
}

fun TextView.setTextWithIcon(str: String, iconRes: Int?) {
    text = iconRes?.let {
        paint.fontMetricsInt
                .let { -it.ascent }
                .let { dim ->
                    resources.getDrawable(iconRes)
                            .apply { setBounds(0, 0, dim, dim) }
                            .let { ImageSpan(it, ALIGN_BASELINE) }
                            .let { SpannableString("  $str").apply { setSpan(it, 0, 1, 0) } }
                }
    } ?: str
}