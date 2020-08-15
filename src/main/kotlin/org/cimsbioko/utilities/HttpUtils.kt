package org.cimsbioko.utilities

import android.util.Base64
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object HttpUtils {

    /**
     * Encodes an HTTP Basic authentication header for the specified username
     * and password.
     *
     * FIXME: password exposed to memory scan as String via constant pool
     */
    fun encodeBasicCreds(username: String, password: String): String =
            "Basic ${Base64.encodeToString("$username:$password".toByteArray(), Base64.DEFAULT)}"

    /**
     * Encodes an HTTP Bearer authentication header for the specified token.
     *
     * FIXME: password exposed to memory scan as String via constant pool
     */
    fun encodeBearerCreds(token: String): String = "Bearer $token"

    /**
     * Constructs an [HttpURLConnection] with the given request headers.
     */
    @Throws(IOException::class)
    operator fun get(url: URL, headers: Map<String?, String?>): HttpURLConnection {
        HttpURLConnection.setFollowRedirects(false)
        val c = url.openConnection() as HttpURLConnection
        for ((key, value) in headers) {
            c.addRequestProperty(key, value)
        }
        return c
    }

    /**
     * Constructs an [HttpURLConnection] with the specified settings, if non-null.
     */
    @Throws(IOException::class)
    operator fun get(url: URL, accept: String?, auth: String?, eTag: String?): HttpURLConnection {
        val headers: MutableMap<String?, String?> = HashMap()
        if (accept != null) {
            headers["Accept"] = accept
        }
        if (auth != null) {
            headers["Authorization"] = auth
        }
        if (eTag != null) {
            headers["If-None-Match"] = eTag
        }
        return get(url, headers)
    }
}