package org.openhds.mobile.task.http;

import android.os.AsyncTask;

import org.openhds.mobile.utilities.HttpUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.openhds.mobile.utilities.SyncUtils.streamToFile;

/**
 * Carry out an HttpTaskRequest.
 * <p>
 * Make an HTTP GET request with credentials, return response status and body.
 * <p>
 * BSH
 */
public class HttpTask extends AsyncTask<HttpTaskRequest, Void, HttpTaskResponse> {

    public enum Result {
        CONNECT_FAILURE,
        STREAM_FAILURE,
        SUCCESS,
        UNMODIFIED,
        AUTH_ERROR,
        CLIENT_ERROR,
        SERVER_ERROR
    }

    private HttpTaskResponseHandler httpTaskResponseHandler;

    // Require a handler to receive http results.
    public HttpTask(HttpTaskResponseHandler httpTaskResponseHandler) {
        this.httpTaskResponseHandler = httpTaskResponseHandler;
    }

    /*
        HTTP requests are now issued by HttpURLConnection, the recommended method for android > 2.3
        URLs with the 'https' scheme return the HttpsURLConnection subclass automatically.
     */
    @Override
    protected HttpTaskResponse doInBackground(HttpTaskRequest... httpTaskRequests) {

        if (httpTaskRequests == null || httpTaskRequests.length == 0) {
            return null;
        }

        final HttpTaskRequest req = httpTaskRequests[0];

        InputStream responseStream = null;
        int statusCode = 0;
        HttpURLConnection conn;
        try {
            URL url = new URL(req.getUrl());
            conn = HttpUtils.get(url, req.getAccept(), req.getAuth(), req.getETag());
            statusCode = conn.getResponseCode();
        } catch (Exception e) {
            return new HttpTaskResponse(false, Result.CONNECT_FAILURE, statusCode, null);
        }

        if (HTTP_OK == statusCode) {
            String eTag = conn.getHeaderField("ETag"), contentType = conn.getContentType();
            File saveFile = req.getFile();
            if (saveFile != null) {
                try {
                    responseStream = conn.getInputStream();
                    streamToFile(responseStream, saveFile);
                    responseStream = new BufferedInputStream(new FileInputStream(saveFile));
                } catch (InterruptedException | IOException e) {
                    return new HttpTaskResponse(false, Result.STREAM_FAILURE, statusCode, responseStream, eTag, contentType);
                }
            }
            return new HttpTaskResponse(true, Result.SUCCESS, statusCode, responseStream, eTag, contentType);
        }

        if (HTTP_NOT_MODIFIED == statusCode) {
            return new HttpTaskResponse(false, Result.UNMODIFIED, statusCode);
        }

        if (statusCode < HTTP_INTERNAL_ERROR) {
            if (statusCode == HTTP_UNAUTHORIZED || statusCode == HTTP_FORBIDDEN) {
                return new HttpTaskResponse(false, Result.AUTH_ERROR, statusCode);
            } else {
                return new HttpTaskResponse(false, Result.CLIENT_ERROR, statusCode);
            }
        }

        return new HttpTaskResponse(false, Result.SERVER_ERROR, statusCode);
    }

    // Forward the Http response to the handler.
    @Override
    protected void onPostExecute(HttpTaskResponse httpTaskResponse) {
        if (null != httpTaskResponseHandler) {
            httpTaskResponseHandler.handleHttpTaskResponse(httpTaskResponse);
        }
    }

    // A handler type to receive response status code and response body input stream.
    public interface HttpTaskResponseHandler {
        void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse);
    }
}
