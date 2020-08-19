package org.cimsbioko.utilities

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import org.cimsbioko.R
import org.cimsbioko.model.form.FormInstance
import org.cimsbioko.model.form.LoadedFormInstance
import org.cimsbioko.navconfig.forms.FormDisplay
import org.cimsbioko.navconfig.forms.FormFormatter
import org.cimsbioko.navconfig.forms.KnownFields
import org.jdom2.Document
import org.jdom2.Element


// Create a new Layout that contains two text views and optionally several "payload" text views beneath.
fun makeText(activity: Activity, layoutTag: Any? = null, listener: View.OnClickListener? = null,
             container: ViewGroup? = null, background: Int = 0): RelativeLayout =
        activity.layoutInflater.inflate(R.layout.generic_list_item_white_text, null)
                .let { it as RelativeLayout }
                .apply {
                    tag = layoutTag
                    listener?.also { setOnClickListener(it) }
                    container?.addView(this)
                    background.takeIf { it != 0 }?.also { setBackgroundResource(it) }
                }

// Pass new data to a layout that was created with makeTextWithPayload().
fun RelativeLayout.configureText(activity: Activity, primaryText: String? = null,
                                 secondaryText: String? = null, stringsPayload: Map<Int, String?>? = null,
                                 centerText: Boolean = true) {

    fun TextView.configure(s: String?) {
        s?.let { text = it }
        visibility = if (s == null) View.GONE else View.VISIBLE
        gravity = Gravity.CENTER
        setPadding(0, 0, 0, 0)
    }

    findViewById<TextView>(R.id.primary_text).configure(primaryText)
    findViewById<TextView>(R.id.secondary_text).configure(secondaryText)
    findViewById<LinearLayout>(R.id.pay_load_container).apply {
        removeAllViews()
        stringsPayload?.also { sp ->
            for (key in sp.keys) {
                addView(makeSmallTextWithLabel(activity).apply {
                    configureTextWithLabel(labelId = key, valueText = sp[key])
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                })
            }
        }
        visibility = if (childCount == 0) View.GONE else View.VISIBLE
        gravity = if (centerText) Gravity.CENTER else Gravity.NO_GRAVITY
        setPadding(if (centerText) 0 else 15, 0, 0, 0)
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

private val CharSequence?.isBlank: Boolean
    get() = this == null || isEmpty() || this == "null"

// Pass new data to text views created with makeLargeTextWithValueAndLabel().
fun RelativeLayout.configureTextWithLabel(labelId: Int, valueText: String?,
                                          labelColorId: Int = R.color.Black, valueColorId: Int = R.color.Black,
                                          missingColorId: Int = R.color.LightGray) {
    val resources = context.resources
    findViewById<TextView>(R.id.label_text)?.apply {
        setText(labelId)
        setTextColor(resources.getColor(if (valueText.isBlank) missingColorId else labelColorId))
    }
    findViewById<TextView>(R.id.delimiter_text)?.apply {
        setTextColor(resources.getColor(if (valueText.isBlank) missingColorId else labelColorId))
    }
    findViewById<TextView>(R.id.value_text)?.apply {
        text = valueText
        setTextColor(resources.getColor(if (valueText.isBlank) missingColorId else valueColorId))
        if (valueText.isBlank) setText(R.string.not_available)
    }
}

fun RelativeLayout.configureTextWithLabel(labelText: String, valueText: String?,
                                          labelColorId: Int = R.color.Black, valueColorId: Int = R.color.Black,
                                          missingColorId: Int = R.color.LightGray) {
    val resources = context.resources
    findViewById<TextView>(R.id.label_text)?.apply {
        text = labelText
        setTextColor(resources.getColor(if (valueText.isBlank) missingColorId else labelColorId))
    }
    findViewById<TextView>(R.id.delimiter_text)?.apply {
        setTextColor(resources.getColor(if (valueText.isBlank) missingColorId else labelColorId))
    }
    findViewById<TextView>(R.id.value_text)?.apply {
        text = valueText
        setTextColor(resources.getColor(if (valueText.isBlank) missingColorId else valueColorId))
        if (valueText.isBlank) setText(R.string.not_available)
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
        text = binding?.label ?: instance.formName
    }

    (binding?.formatter ?: LegacyFormatter).format(doc).also {
        findViewById<TextView>(R.id.form_instance_list_id)?.text = it.entity ?: ""
        findViewById<TextView>(R.id.form_instance_list_fieldworker)?.text = it.fieldworker ?: ""
        findViewById<TextView>(R.id.form_instance_list_date)?.text = it.dateTimeCollected ?: ""
        findViewById<TextView>(R.id.form_instance_list_extra1)?.text = it.extra1 ?: ""
        findViewById<TextView>(R.id.form_instance_list_extra2)?.text = it.extra2 ?: ""
    }
}

/**
 * Provided for older campaigns that do not define their formatter. This can be retired once we migrate older campaigns.
 */
object LegacyFormatter : FormFormatter {
    override fun format(dataDoc: Document): FormDisplay = LegacyDisplay(dataDoc.rootElement)
}

class LegacyDisplay(e: Element) : FormDisplay {
    override val fieldworker: String? = e.getChildText(KnownFields.FIELD_WORKER_EXTID)
    override val entity: String? = e.getChildText(KnownFields.ENTITY_EXTID)
    override val dateTimeCollected: String? = e.getChildText(KnownFields.COLLECTION_DATE_TIME)
    override val extra1: String? = null
    override val extra2: String? = null
}