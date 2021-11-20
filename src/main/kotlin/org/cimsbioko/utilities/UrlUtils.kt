package org.cimsbioko.utilities

import android.content.Context
import androidx.core.content.edit
import org.cimsbioko.R
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

object UrlUtils {

    fun buildServerUrl(context: Context, path: String): String? {
        val baseUrl = getServerBaseUrl(context)
        if (baseUrl.trim { it <= ' ' }.isEmpty()) {
            MessageUtils.showLongToast(context, R.string.no_server_url)
            return null
        }
        return baseUrl + path
    }

    private fun getServerBaseUrl(context: Context) =
            ConfigUtils.getPreferenceString(context, R.string.server_url_key, context.getString(R.string.default_server_url))
                    ?: "no server url found"

    fun setServerUrl(context: Context, url: String?) {
        ConfigUtils.getSharedPrefs(context).edit { putString(context.getString(R.string.server_url_key), url) }
    }

    fun urlDecode(value: String): String {
        return try {
            URLDecoder.decode(value, "UTF8")
        } catch (e: UnsupportedEncodingException) {
            value
        }
    }
}