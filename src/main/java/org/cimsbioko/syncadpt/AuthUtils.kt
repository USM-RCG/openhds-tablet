package org.cimsbioko.syncadpt

import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import org.cimsbioko.R
import org.cimsbioko.utilities.HttpUtils.encodeBasicCreds
import org.cimsbioko.utilities.IOUtils.copy
import org.cimsbioko.utilities.UrlUtils.buildServerUrl
import org.cimsbioko.utilities.use
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object AuthUtils {

    /**
     * Calls server web api to register the device (initial association).
     *
     * @param ctx      used to get the registration endpoint relative to server url in settings
     * @param name     the device 'username' to use when authenticating to device api
     * @param secret the secret to use when authenticating
     * @return a [JSONObject] containing the response data.
     * @throws IOException   when url is bad, or io fails
     * @throws JSONException when construction of the response object fails
     */
    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun register(ctx: Context, name: String, secret: String) = getRegistrationEndpoint(ctx).openConnection()
            .apply {
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", encodeBasicCreds(name, secret))
            }.let { c ->
                with(c as HttpURLConnection) {
                    try {
                        doOutput = true
                        setChunkedStreamingMode(0)
                        BufferedOutputStream(outputStream).use {
                            it.apply {
                                write(deviceDescription.toByteArray())
                                flush()
                            }
                        }
                        toJsonObject()
                    } finally {
                        disconnect()
                    }
                }
            }


    /**
     * Calls server web api to obtain a new access token for the device, in other words - a refreshed token.
     *
     * @param ctx      used to get the token endpoint relative to server url in settings
     * @param name     the device 'username' to use when authenticating to device api
     * @param secret the secret to use when authenticating
     * @return a [JSONObject] containing the response data.
     * @throws IOException   when url is bad, or io fails
     * @throws JSONException when construction of the response object fails
     */
    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun token(ctx: Context, name: String, secret: String) = getTokenEndpoint(ctx).openConnection()
            .let { it as HttpURLConnection }
            .apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", encodeBasicCreds(name, secret))
            }
            .let {
                try {
                    it.toJsonObject()
                } finally {
                    it.disconnect()
                }
            }

    /**
     * Returns a device description suitable for general differentiation of device types. This is used to help device
     * administrators identify and manage registered devices with more information than just the device name.
     *
     * @return a description including manufacturer, brand, model, and Android release.
     * @throws JSONException
     */
    @get:Throws(JSONException::class)
    val deviceDescription: String
        get() = JSONObject()
                .put("description", buildString {
                    for (s in listOf(Build.MANUFACTURER, Build.BRAND, Build.MODEL, "Android ${VERSION.RELEASE}")) {
                        if (Build.UNKNOWN != s) {
                            append(s)
                            append("\n")
                        }
                    }
                })
                .toString()

    /**
     * Returns the server [URL] to use to register as a device. It is constructed based on the application's
     * configured server endpoint.
     *
     * @param ctx application context to use for relevant config values
     * @return a [URL] object corresponding to the device registration endpoint
     * @throws MalformedURLException when the constructed value is not a valid URL
     */
    @Throws(MalformedURLException::class)
    fun getRegistrationEndpoint(ctx: Context): URL = URL(buildServerUrl(ctx, ctx.getString(R.string.auth_registration_path)))

    /**
     * Returns the server [URL] to use to issue access tokens as a device. It is constructed relative to the
     * application's configured server endpoint.
     *
     * @param ctx application context to use for relevant config values
     * @return a [URL] object corresponding to the device token endpoint
     * @throws MalformedURLException when the constructed value is not a valid URL
     */
    @Throws(MalformedURLException::class)
    fun getTokenEndpoint(ctx: Context): URL = URL(buildServerUrl(ctx, ctx.getString(R.string.auth_token_path)))
}

private fun HttpURLConnection.toJsonObject(): JSONObject =
        if (responseCode != HttpURLConnection.HTTP_OK || !contentType.startsWith("application/json")) {
            throw RuntimeException("unexpected response: status $responseCode, mime type = $contentType")
        } else {
            val bis = BufferedInputStream(inputStream)
            val bout = ByteArrayOutputStream()
            listOf(bis, bout).use {
                copy(bis, bout)
                JSONObject(bout.toString())
            }
        }

