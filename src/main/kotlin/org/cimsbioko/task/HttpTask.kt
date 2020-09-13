package org.cimsbioko.task

import android.os.AsyncTask
import org.cimsbioko.utilities.HttpUtils.get
import org.cimsbioko.utilities.IOUtils.streamToFile
import org.cimsbioko.utilities.IOUtils.streamToStream
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Carry out an HttpTaskRequest.  Make an HTTP GET request with credentials, return response status and body.
 */
class HttpTask(private val handler: HttpTaskResponseHandler) : AsyncTask<HttpTaskRequest, Void, HttpTaskResponse>() {

    enum class Result {
        CONNECT_FAILURE, STREAM_FAILURE, SUCCESS, UNMODIFIED, AUTH_ERROR, CLIENT_ERROR, SERVER_ERROR
    }

    /*
        HTTP requests are now issued by HttpURLConnection, the recommended method for android > 2.3
        URLs with the 'https' scheme return the HttpsURLConnection subclass automatically.
     */
    override fun doInBackground(vararg requests: HttpTaskRequest): HttpTaskResponse {
        val req = requests.first()
        var responseStream: InputStream?
        var statusCode = 0
        val conn: HttpURLConnection
        try {
            val url = URL(req.url)
            conn = get(url, req.accept, req.auth, req.eTag)
            statusCode = conn.responseCode
        } catch (e: Exception) {
            return HttpTaskResponse(false, Result.CONNECT_FAILURE, statusCode, null)
        }
        if (HttpURLConnection.HTTP_OK == statusCode) {
            val saveFile = req.file
            return try {
                responseStream = conn.inputStream
                responseStream = if (saveFile != null) {
                    streamToFile(responseStream, saveFile)
                    BufferedInputStream(FileInputStream(saveFile))
                } else {
                    val out = ByteArrayOutputStream()
                    streamToStream(responseStream, out, null)
                    ByteArrayInputStream(out.toByteArray())
                }
                HttpTaskResponse(true, Result.SUCCESS, statusCode, responseStream, conn.headerFields)
            } catch (e: InterruptedException) {
                HttpTaskResponse(false, Result.STREAM_FAILURE, statusCode)
            } catch (e: IOException) {
                HttpTaskResponse(false, Result.STREAM_FAILURE, statusCode)
            }
        }
        if (HttpURLConnection.HTTP_NOT_MODIFIED == statusCode) {
            return HttpTaskResponse(false, Result.UNMODIFIED, statusCode)
        }
        return if (statusCode < HttpURLConnection.HTTP_INTERNAL_ERROR) {
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED || statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                HttpTaskResponse(false, Result.AUTH_ERROR, statusCode)
            } else {
                HttpTaskResponse(false, Result.CLIENT_ERROR, statusCode)
            }
        } else HttpTaskResponse(false, Result.SERVER_ERROR, statusCode)
    }

    // Forward the Http response to the handler.
    override fun onPostExecute(httpTaskResponse: HttpTaskResponse) {
        handler.handleHttpTaskResponse(httpTaskResponse)
    }

    // A handler type to receive response status code and response body input stream.
    interface HttpTaskResponseHandler {
        fun handleHttpTaskResponse(httpTaskResponse: HttpTaskResponse)
    }

}

class HttpTaskRequest constructor(
        val url: String,
        val accept: String,
        val auth: String,
        val eTag: String? = null,
        val file: File? = null
)

class HttpTaskResponse internal constructor(
        val isSuccess: Boolean,
        val result: HttpTask.Result,
        val httpStatus: Int,
        val inputStream: InputStream? = null,
        private val headers: Map<String, List<String>> = HashMap()
) {

    private fun getHeader(name: String): String? {
        if (headers.containsKey(name)) {
            val values: List<String>? = headers[name]
            if (values != null && values.isNotEmpty()) {
                return values[0]
            }
        }
        return null
    }

    val eTag: String?
        get() = getHeader("etag")

    val contentType: String?
        get() = getHeader("content-type")

}