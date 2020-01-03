package org.cimsbioko.task.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpTaskResponse {

    private final boolean isSuccess;
    private final int httpStatus;
    private final HttpTask.Result result;
    private final InputStream inputStream;
    private final Map<String, List<String>> headers;

    HttpTaskResponse(boolean isSuccess, HttpTask.Result result, int httpStatus) {
        this(isSuccess, result, httpStatus, null);
    }

    HttpTaskResponse(boolean isSuccess, HttpTask.Result result, int httpStatus, InputStream inputStream) {
        this(isSuccess, result, httpStatus, inputStream, new HashMap<>());
    }

    HttpTaskResponse(boolean isSuccess, HttpTask.Result result, int httpStatus, InputStream inputStream, Map<String, List<String>> headers) {
        this.isSuccess = isSuccess;
        this.result = result;
        this.httpStatus = httpStatus;
        this.inputStream = inputStream;
        this.headers = headers;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public HttpTask.Result getResult() {
        return result;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getETag() {
        return getHeader("etag");
    }

    public String getHeader(String name) {
        if (headers.containsKey(name)) {
            List<String> values = headers.get(name);
            if (values != null && values.size() > 0) {
                return values.get(0);
            }
        }
        return null;
    }

    public String getContentType() {
        return getHeader("content-type");
    }
}
