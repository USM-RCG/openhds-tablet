package org.cimsbioko.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.cimsbioko.R
import org.cimsbioko.model.form.LoadedFormInstance
import org.cimsbioko.utilities.LayoutUtils.configureFormListItem

class FormInstanceAdapter(
        context: Context,
        resource: Int,
        private val instances: MutableList<LoadedFormInstance>
) : ArrayAdapter<LoadedFormInstance>(context, resource, instances) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (convertView ?: inflater.inflate(R.layout.form_instance_list_item, null)).also {
                configureFormListItem(it, instances[position])
            }
}