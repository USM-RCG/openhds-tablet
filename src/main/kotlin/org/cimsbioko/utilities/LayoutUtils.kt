package org.cimsbioko.utilities

import android.app.Activity
import android.text.SpannableString
import android.text.style.DynamicDrawableSpan.ALIGN_BASELINE
import android.text.style.ImageSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import org.cimsbioko.R
import org.cimsbioko.databinding.GenericListItemBinding
import org.cimsbioko.databinding.ValueWithLabelBinding
import org.cimsbioko.navconfig.Hierarchy


// Create a new Layout that contains two text views and optionally several key-value text views beneath.
fun makeText(activity: Activity, layoutTag: Any? = null, listener: View.OnClickListener? = null,
             container: ViewGroup? = null, background: Int = 0): GenericListItemBinding =
        GenericListItemBinding.inflate(activity.layoutInflater)
                .also { binding ->
                    binding.root.apply {
                        tag = layoutTag
                        listener?.also { setOnClickListener(it) }
                        container?.addView(this)
                        background.takeIf { it != 0 }?.also { setBackgroundResource(it) }
                    }
                }


// Pass new data to a layout that was created with makeTextWithPayload().
fun GenericListItemBinding.configureText(activity: Activity, text1: String? = null,
                                         text2: String? = null, details: Map<String, String?>? = null,
                                         centerText: Boolean = true, iconRes: Int? = null, detailsPadding: Int = 0) {

    fun TextView.configure(s: String?, iconRes: Int? = null) {
        s?.let { str -> setTextWithIcon(str, iconRes) }
        visibility = if (s == null) View.GONE else View.VISIBLE
        gravity = Gravity.CENTER
    }

    primaryText.configure(text1, iconRes)
    secondaryText.configure(text2)
    detailsContainer.apply {
        removeAllViews()
        details?.also { sp ->
            for ((key, value) in sp) {
                if (!value.isBlank) {
                    addView(makeTextWithLabel(activity).apply {
                        configureTextWithLabel(label = key, value = value)
                    }.root.apply {
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
fun makeTextWithLabel(activity: Activity): ValueWithLabelBinding = ValueWithLabelBinding.inflate(activity.layoutInflater)

val CharSequence?.isBlank: Boolean
    get() = this == null || isEmpty() || this == "null"

fun ValueWithLabelBinding.configureTextWithLabel(label: String, value: String?,
                                                 labelColorId: Int = R.color.Black, valueColorId: Int = R.color.Black) {
    labelText.apply {
        text = label
        setTextColor(ContextCompat.getColor(context, labelColorId))
    }
    valueText.apply {
        text = value
        setTextColor(ContextCompat.getColor(context, valueColorId))
    }
}

fun String?.toLevelIcon(): Int? {
    return this?.let {
        when (it) {
            Hierarchy.HOUSEHOLD -> R.drawable.ic_household
            Hierarchy.INDIVIDUAL -> R.drawable.ic_individual
            else -> R.drawable.ic_hierarchy
        }
    }
}

fun TextView.setTextWithIcon(str: String, iconRes: Int?, color: Int? = null) {
    text = iconRes?.let { res ->
        paint.fontMetricsInt
                .let { -it.ascent }
                .let { dim ->
                    AppCompatResources.getDrawable(context, res)
                            ?.let { DrawableCompat.wrap(it) }
                            ?.let { drawable ->
                                color?.let { drawable.mutate().also { DrawableCompat.setTint(it, color) } } ?: drawable
                            }
                            ?.apply { setBounds(0, 0, dim, dim) }
                            ?.let { ImageSpan(it, ALIGN_BASELINE) }
                            ?.let { SpannableString("  $str").apply { setSpan(it, 0, 1, 0) } }
                }
    } ?: str
}