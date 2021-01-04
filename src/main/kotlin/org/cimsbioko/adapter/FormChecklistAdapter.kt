package org.cimsbioko.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.core.view.get
import org.cimsbioko.R
import org.cimsbioko.databinding.FormInstanceCheckItemBinding
import org.cimsbioko.databinding.FormInstanceListItemBinding
import org.cimsbioko.model.LoadedFormInstance
import org.cimsbioko.utilities.FormUtils.editIntent
import org.cimsbioko.utilities.MessageUtils.showShortToast

class FormChecklistAdapter(
    context: Context,
    checklistItemId: Int,
    formInstances: MutableList<LoadedFormInstance>
) : ArrayAdapter<LoadedFormInstance>(context, checklistItemId, formInstances) {

    companion object {
        const val CHECK_STATES_KEY = "flcastates"
    }

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val instances: MutableList<LoadedFormInstance> = formInstances
    private var checked: MutableSet<Int> = HashSet()

    fun restoreCheckStates(bundle: Bundle) {
        bundle.getIntArray(CHECK_STATES_KEY)?.also { checked = it.toHashSet() }
    }

    fun saveCheckStates(bundle: Bundle) {
        bundle.putIntArray(CHECK_STATES_KEY, checked.toIntArray())
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

            // expand checkbox touch target
            binding.root.post {
                binding.formInstanceCheckBox.also { checkbox ->
                    (checkbox.parent as? View)?.apply {
                        touchDelegate = TouchDelegate(Rect().also { checkbox.getHitRect(it) }.apply {
                            context.resources.getDimensionPixelSize(R.dimen.instance_checkbox_padding).also { pad -> inset(-pad, -pad) }
                        }, checkbox)
                    }
                }
            }
        }

        binding.formInstanceCheckBox.apply {
            isChecked = position in checked
            setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                if (isChecked) checked.add(position) else checked.remove(position)
            }
        }

        return binding.root
    }

    val checkedInstances: List<LoadedFormInstance>
        get() = checked.mapNotNull { getItem(it) }

    fun removeAll(instances: List<LoadedFormInstance>): Boolean {
        try {
            setNotifyOnChange(false)
            checked.removeAll(instances.map { getPosition(it) }.filterNot { it == -1 })
            return if (this.instances.removeAll(instances)) {
                notifyDataSetChanged()
                true
            } else false
        } finally {
            setNotifyOnChange(true)
        }
    }
}

private fun FormInstanceCheckItemBinding.configureForInstance(instance: LoadedFormInstance) {
    FormInstanceListItemBinding.bind(formInstanceItemArea[0]).configureForInstance(instance)
}