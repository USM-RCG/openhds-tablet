package org.cimsbioko.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.cimsbioko.R
import org.cimsbioko.model.FormInstance
import org.cimsbioko.model.LoadedFormInstance
import org.cimsbioko.utilities.setTextWithIcon

class FormInstanceAdapter(
        context: Context,
        resource: Int,
        private val instances: MutableList<LoadedFormInstance>
) : ArrayAdapter<LoadedFormInstance>(context, resource, instances) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (convertView ?: inflater.inflate(R.layout.form_instance_list_item, null)).also {
                it.configureFormListItem(instances[position])
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