package org.cimsbioko.utilities

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import org.cimsbioko.R
import org.cimsbioko.model.form.FormInstance
import org.cimsbioko.navconfig.forms.KnownFields
import org.jdom2.JDOMException
import java.io.IOException

object LayoutUtils {

    private val TAG = LayoutUtils::class.java.simpleName

    // Create a new Layout that contains two text views and optionally several "payload" text views beneath.
    @JvmStatic
    fun makeTextWithPayload(activity: Activity, primaryText: String?, secondaryText: String?,
                            layoutTag: Any?, listener: View.OnClickListener?, container: ViewGroup?,
                            background: Int, stringsPayLoad: Map<Int, String?>?,
                            stringsIdsPayLoad: Map<Int, Int?>?, centerText: Boolean): RelativeLayout {
        val layout = activity.layoutInflater.inflate(R.layout.generic_list_item_white_text, null) as RelativeLayout
        layout.tag = layoutTag
        if (listener != null) {
            layout.setOnClickListener(listener)
        }
        container?.addView(layout)
        if (background != 0) {
            layout.setBackgroundResource(background)
        }
        configureTextWithPayload(activity, layout, primaryText, secondaryText, stringsPayLoad, stringsIdsPayLoad, centerText)
        return layout
    }

    // Pass new data to a layout that was created with makeTextWithPayload().
    @JvmStatic
    fun configureTextWithPayload(activity: Activity, layout: RelativeLayout, primaryText: String?,
                                 secondaryText: String?, stringsPayload: Map<Int, String?>?,
                                 stringsIdsPayload: Map<Int, Int?>?, centerText: Boolean) {
        val primary = layout.findViewById<TextView>(R.id.primary_text)
        val secondary = layout.findViewById<TextView>(R.id.secondary_text)
        val payLoadContainer = layout.findViewById<LinearLayout>(R.id.pay_load_container)
        if (primaryText == null) {
            primary.visibility = View.GONE
        } else {
            primary.visibility = View.VISIBLE
            primary.text = primaryText
        }
        if (secondaryText == null) {
            secondary.visibility = View.GONE
        } else {
            secondary.visibility = View.VISIBLE
            secondary.text = secondaryText
        }

        // fill in payload strings, if any
        payLoadContainer.removeAllViews()
        if (stringsPayload != null) {
            for (key in stringsPayload.keys) {
                val value = stringsPayload[key] ?: continue
                val relativeLayout = makeSmallTextWithValueAndLabel(activity, key, value, R.color.Black, R.color.Black, R.color.LightGray)
                relativeLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                payLoadContainer.addView(relativeLayout)
            }
        }
        if (stringsIdsPayload != null) {
            for (key in stringsIdsPayload.keys) {
                val value = activity.resources.getString(stringsIdsPayload[key]!!) ?: continue
                val relativeLayout = makeSmallTextWithValueAndLabel(activity, key, value, R.color.Black, R.color.Black, R.color.LightGray)
                relativeLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                payLoadContainer.addView(relativeLayout)
            }
        }
        if (payLoadContainer.childCount == 0) {
            payLoadContainer.visibility = View.GONE
        } else {
            payLoadContainer.visibility = View.VISIBLE
        }
        primary.gravity = Gravity.CENTER
        primary.setPadding(0, 0, 0, 0)
        secondary.gravity = Gravity.CENTER
        secondary.setPadding(0, 0, 0, 0)
        if (centerText) {
            payLoadContainer.gravity = Gravity.CENTER
            payLoadContainer.setPadding(0, 0, 0, 0)
        } else {
            payLoadContainer.gravity = Gravity.NO_GRAVITY
            payLoadContainer.setPadding(15, 0, 0, 0)
        }
    }

    // Create a pair of text views to represent some value plus its label, with given colors.
    @JvmStatic
    fun makeLargeTextWithValueAndLabel(activity: Activity, labelId: Int, valueText: String?,
                                       labelColorId: Int, valueColorId: Int, missingColorId: Int): RelativeLayout {
        val layout = activity.layoutInflater.inflate(R.layout.value_with_label_large, null) as RelativeLayout
        configureTextWithValueAndLabel(layout, labelId, valueText, labelColorId, valueColorId, missingColorId)
        return layout
    }

    // Create a pair of text views to represent some value plus its label, with given colors.
    private fun makeSmallTextWithValueAndLabel(activity: Activity, labelId: Int, valueText: String?,
                                               labelColorId: Int, valueColorId: Int, missingColorId: Int): RelativeLayout {
        val layout = activity.layoutInflater.inflate(R.layout.value_with_label_small, null) as RelativeLayout
        configureTextWithValueAndLabel(layout, labelId, valueText, labelColorId, valueColorId, missingColorId)
        return layout
    }

    // Pass new data to text views created with makeLargeTextWithValueAndLabel().
    private fun configureTextWithValueAndLabel(layout: RelativeLayout, labelId: Int, valueText: String?,
                                               labelColorId: Int, valueColorId: Int, missingColorId: Int) {
        val labelTextView = layout.findViewById<TextView>(R.id.label_text)
        val delimiterTextView = layout.findViewById<TextView>(R.id.delimiter_text)
        val valueTextView = layout.findViewById<TextView>(R.id.value_text)
        labelTextView.setText(labelId)
        valueTextView.text = valueText
        val context = layout.context
        if (null == valueText || valueText.isEmpty() || valueText == "null") {
            labelTextView.setTextColor(context.resources.getColor(missingColorId))
            delimiterTextView.setTextColor(context.resources.getColor(missingColorId))
            valueTextView.setTextColor(context.resources.getColor(missingColorId))
            valueTextView.setText(R.string.not_available)
        } else {
            labelTextView.setTextColor(context.resources.getColor(labelColorId))
            delimiterTextView.setTextColor(context.resources.getColor(labelColorId))
            valueTextView.setTextColor(context.resources.getColor(valueColorId))
        }
    }

    // Set up a form list item based on a given form instance.
    @JvmStatic
    fun configureFormListItem(view: View, instance: FormInstance) {
        try {
            val dataDoc = instance.load()
            if (instance.isComplete) {
                if (instance.canEdit()) {
                    view.setBackgroundResource(R.drawable.form_list)
                } else {
                    view.setBackgroundResource(R.drawable.form_list_locked)
                }
            } else {
                view.setBackgroundResource(R.drawable.form_list_gray)
            }

            // Set form name based on its embedded binding
            val formTypeName = if (FormInstance.getBinding(dataDoc) != null) FormInstance.getBinding(dataDoc).label else instance.formName
            val formTypeView = view.findViewById<TextView>(R.id.form_instance_list_type)
            formTypeView.text = formTypeName
            val data = dataDoc.rootElement

            // Extract and set values contained within the form instance
            val entityId = data.getChildText(KnownFields.ENTITY_EXTID)
            setText(view.findViewById(R.id.form_instance_list_id), entityId)
            val fieldWorker = data.getChildText(KnownFields.FIELD_WORKER_EXTID)
            setText(view.findViewById(R.id.form_instance_list_fieldworker), fieldWorker)
            val date = data.getChildText(KnownFields.COLLECTION_DATE_TIME)
            setText(view.findViewById(R.id.form_instance_list_date), date)
        } catch (e: IOException) {
            view.setBackgroundResource(R.drawable.form_list_red)
            Log.w(TAG, e.message)
        } catch (e: JDOMException) {
            view.setBackgroundResource(R.drawable.form_list_red)
            Log.w(TAG, e.message)
        }
    }

    private fun setText(view: View, value: String?) {
        (view as TextView).text = value ?: ""
    }
}