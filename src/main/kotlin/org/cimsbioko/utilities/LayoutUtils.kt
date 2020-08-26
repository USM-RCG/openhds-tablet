package org.cimsbioko.utilities

import android.app.Activity
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import org.cimsbioko.R
import org.cimsbioko.model.form.FormInstance
import org.cimsbioko.model.form.LoadedFormInstance
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
                    addView(makeSmallTextWithLabel(activity).apply {
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
fun makeLargeTextWithLabel(activity: Activity): RelativeLayout =
        activity.layoutInflater.inflate(R.layout.value_with_label_large, null)
                .let { it as RelativeLayout }

// Create a pair of text views to represent some value plus its label, with given colors.
private fun makeSmallTextWithLabel(activity: Activity): RelativeLayout =
        activity.layoutInflater.inflate(R.layout.value_with_label_small, null)
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
    findViewById<TextView>(R.id.delimiter_text)?.apply {
        setTextColor(resources.getColor(labelColorId))
    }
    findViewById<TextView>(R.id.value_text)?.apply {
        text = valueText
        setTextColor(resources.getColor(valueColorId))
    }
}

// Set up a form list item based on a given form instance.
fun View.configureFormListItem(instance: LoadedFormInstance) {

    setBackgroundResource(when {
        instance.isComplete && instance.isEditable -> R.drawable.form_list
        instance.isComplete && !instance.isEditable -> R.drawable.form_list_locked
        else -> R.drawable.form_list_gray
    })

    val doc = instance.document
    val binding = FormInstance.getBinding(doc)

    // Set form name based on its embedded binding
    findViewById<TextView>(R.id.form_instance_list_type)?.apply {
        setTextWithIcon(binding?.label ?: instance.formName, R.drawable.ic_form)
    }

    binding?.formatter?.format(doc)?.also {
        findViewById<TextView>(R.id.form_instance_list_id)?.text = it.entity ?: ""
        findViewById<TextView>(R.id.form_instance_list_fieldworker)?.text = it.fieldworker ?: ""
        findViewById<TextView>(R.id.form_instance_list_date)?.text = it.dateTimeCollected ?: ""
        findViewById<TextView>(R.id.form_instance_list_extra1)?.text = it.extra1 ?: ""
        findViewById<TextView>(R.id.form_instance_list_extra2)?.text = it.extra2 ?: ""
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
        resources.getDrawable(iconRes)
                .apply { setBounds(0, 0, lineHeight, lineHeight) }
                .let { ImageSpan(it) }
                .let { SpannableString("  $str").apply { setSpan(it, 0, 1, 0) } }
    } ?: str
}