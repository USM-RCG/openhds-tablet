package org.cimsbioko.task.http;

import java.io.InputStream;

public class HttpTaskResponse {

    private final boolean isSuccess;
    private final int httpStatus;
    private final HttpTask.Result result;
    private final InputStream inputStream;
    private final String eTag;
    private final String contentType;

    HttpTaskResponse(boolean isSuccess, HttpTask.Result result, int httpStatus) {
        this(isSuccess, result, httpStatus, null, null);
    }

    HttpTaskResponse(boolean isSuccess, HttpTask.Result result, int httpStatus, InputStream inputStream) {
        this(isSuccess, result, httpStatus, inputStream, null);
    }

    HttpTaskResponse(boolean isSuccess, HttpTask.Result result, int httpStatus, InputStream inputStream, String eTag) {
        this(isSuccess, result, httpStatus, inputStream, eTag, null);
    }

    HttpTaskResponse(boolean isSuccess, HttpTask.Result result, int httpStatus, InputStream inputStream, String eTag, String contentType) {
        this.isSuccess = isSuccess;
        this.result = result;
        this.httpStatus = httpStatus;
        this.inputStream = inputStream;
        this.eTag = eTag;
        this.contentType = contentType;
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
        return eTag;
    }

    public String getContentType() {
        return contentType;
    }
}
