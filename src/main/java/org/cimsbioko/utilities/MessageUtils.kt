package org.cimsbioko.utilities

import android.content.Context
import android.widget.Toast
import org.cimsbioko.utilities.ConfigUtils.getResourceString

object MessageUtils {

    @JvmStatic
    fun showLongToast(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @JvmStatic
    fun showLongToast(context: Context?, messageId: Int) {
        val message = getResourceString(context!!, messageId)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @JvmStatic
    fun showShortToast(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun showShortToast(context: Context?, messageId: Int) {
        val message = getResourceString(context!!, messageId)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}