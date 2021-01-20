package org.cimsbioko.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.cimsbioko.R
import org.cimsbioko.databinding.FormInstanceListItemBinding
import org.cimsbioko.model.FormInstance
import org.cimsbioko.model.LoadedFormInstance
import org.cimsbioko.utilities.setTextWithIcon

class FormInstanceAdapter(
        context: Context,
        resource: Int,
        private val instances: MutableList<LoadedFormInstance>
) : ArrayAdapter<LoadedFormInstance>(context, resource, instances) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = convertView?.tag as? FormInstanceListItemBinding
            ?: FormInstanceListItemBinding.inflate(inflater).apply { root.tag = this }
        binding.configureForInstance(instances[position])
        return binding.root
    }
}

fun FormInstanceListItemBinding.configureForInstance(instance: LoadedFormInstance) {

    root.setBackgroundResource(when {
        instance.isComplete && instance.isEditable -> R.drawable.form_list
        instance.isComplete && !instance.isEditable -> R.drawable.form_list_locked
        else -> R.drawable.form_list_gray
    })

    val doc = instance.document
    val formBinding = FormInstance.getBinding(doc)

    // Set form name based on its embedded binding
    formInstanceListType.setTextWithIcon(formBinding?.label ?: instance.formName, R.drawable.ic_form)

    formBinding?.formatter?.format(doc)?.also {
        formInstanceListId.text = it.entity ?: ""
        formInstanceListFieldworker.text = it.fieldworker ?: ""
        formInstanceListDate.text = it.dateTimeCollected ?: ""
        formInstanceListExtra1.text = it.extra1 ?: ""
        formInstanceListExtra2.text = it.extra2 ?: ""
    }
}