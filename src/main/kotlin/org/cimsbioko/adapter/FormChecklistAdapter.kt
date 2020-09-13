package org.cimsbioko.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.core.view.get
import org.cimsbioko.R
import org.cimsbioko.databinding.FormInstanceCheckItemBinding
import org.cimsbioko.databinding.FormInstanceListItemBinding
import org.cimsbioko.model.FormInstance
import org.cimsbioko.model.LoadedFormInstance
import org.cimsbioko.utilities.FormUtils.editIntent
import org.cimsbioko.utilities.MessageUtils.showShortToast

class FormChecklistAdapter(
        context: Context,
        checklistItemId: Int,
        formInstances: MutableList<LoadedFormInstance>
) : ArrayAdapter<LoadedFormInstance>(context, checklistItemId, formInstances) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val instances: MutableList<LoadedFormInstance> = formInstances
    private var checkStates: MutableList<Boolean> = ArrayList()

    init {
        initCheckStates()
    }

    private fun initCheckStates() {
        checkStates = ArrayList<Boolean>(count).apply { for (i in 0 until count) add(false) }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = convertView?.tag as? FormInstanceCheckItemBinding
                ?: FormInstanceCheckItemBinding.inflate(inflater).apply { root.tag = this }

        instances[position].also { instance ->
            binding.configureForInstance(instance)
            binding.formInstanceItemArea.setOnClickListener {
                this@FormChecklistAdapter.context.also { ctx ->
                    showShortToast(ctx, R.string.launching_form)
                    (ctx as Activity).startActivityForResult(editIntent(instance.uri), 0)
                }
            }
        }

        binding.formInstanceCheckBox.apply {
            setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean -> checkStates[position] = isChecked }
            isChecked = checkStates[position]
        }

        return binding.root
    }

    val checkedInstances: List<FormInstance>
        get() = ArrayList<FormInstance>().apply { for (i in checkStates.indices) if (checkStates[i]) add(getItem(i)!!) }

    override fun add(`object`: LoadedFormInstance?) {
        super.add(`object`)
        checkStates.add(false)
    }

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

private fun FormInstanceCheckItemBinding.configureForInstance(instance: LoadedFormInstance) {
    FormInstanceListItemBinding.bind(formInstanceItemArea[0]).configureForInstance(instance)
}