package org.cimsbioko.task.http;

import android.os.AsyncTask;
import org.cimsbioko.utilities.HttpUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.*;
import static org.cimsbioko.utilities.IOUtils.streamToFile;
import static org.cimsbioko.utilities.IOUtils.streamToStream;

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
            File saveFile = req.getFile();
            try {
                responseStream = conn.getInputStream();
                if (saveFile != null) {
                    streamToFile(responseStream, saveFile);
                    responseStream = new BufferedInputStream(new FileInputStream(saveFile));
                } else {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    streamToStream(responseStream, out, null);
                    responseStream = new ByteArrayInputStream(out.toByteArray());
                }
                return new HttpTaskResponse(true, Result.SUCCESS, statusCode, responseStream, conn.getHeaderFields());
            } catch (InterruptedException | IOException e) {
                return new HttpTaskResponse(false, Result.STREAM_FAILURE, statusCode);
            }
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
