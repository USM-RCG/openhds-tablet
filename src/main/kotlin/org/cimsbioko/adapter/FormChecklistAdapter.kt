package org.cimsbioko.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import org.cimsbioko.R
import org.cimsbioko.model.form.FormInstance
import org.cimsbioko.utilities.FormUtils.editIntent
import org.cimsbioko.utilities.LayoutUtils.configureFormListItem
import org.cimsbioko.utilities.MessageUtils.showShortToast
import java.util.*

class FormChecklistAdapter(
        context: Context,
        checklistItemId: Int,
        formInstances: MutableList<FormInstance>
) : ArrayAdapter<FormInstance>(context, checklistItemId, formInstances) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val instances: MutableList<FormInstance> = formInstances
    private var checkStates: MutableList<Boolean> = emptyList<Boolean>().toMutableList()

    init {
        initCheckStates()
    }

    private fun initCheckStates() {
        checkStates = ArrayList<Boolean>(count).apply { for (i in 0 until count) add(false) }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (convertView ?: inflater.inflate(R.layout.form_instance_check_item, null)).also { view ->
                instances[position].also { instance ->
                    configureFormListItem(view, instance)
                    view.findViewById<ViewGroup>(R.id.form_instance_item_area).also { item ->
                        item.tag = instance
                        item.setOnClickListener { v: View ->
                            this@FormChecklistAdapter.context.also { ctx ->
                                showShortToast(ctx, R.string.launching_form)
                                (v.tag as FormInstance).also {
                                    (ctx as Activity).startActivityForResult(editIntent(it.uri), 0)
                                }
                            }
                        }
                    }
                }

                // add callback when the checkbox is checked
                view.findViewById<CheckBox>(R.id.form_instance_check_box)?.also { box ->
                    box.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean -> checkStates[position] = isChecked }
                    box.isChecked = checkStates[position]
                }
            }

    val checkedInstances: List<FormInstance>
        get() = ArrayList<FormInstance>().apply { for (i in checkStates.indices) if (checkStates[i]) add(getItem(i)!!) }

    fun removeAll(instances: List<FormInstance>): Boolean = try {
        setNotifyOnChange(false)
        this.instances.removeAll(instances).also {
            initCheckStates()
            notifyDataSetChanged()
        }
    } finally {
        setNotifyOnChange(true)
    }

}