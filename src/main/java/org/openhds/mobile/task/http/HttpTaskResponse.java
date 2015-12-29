package org.openhds.mobile.task.http;

import java.io.InputStream;

/**
 * Status and input stream from an HTTP request.
 *
 * Response status code and response body input stream
 * returned from an HttpTask.
 *
 * BSH
 */
public class HttpTaskResponse {

    private final boolean isSuccess;
    private final String message;
    private final int httpStatus;
    private final InputStream inputStream;
    private final String eTag;
    private final String contentType;

    public HttpTaskResponse(boolean isSuccess, String message, int httpStatus, InputStream inputStream) {
        this(isSuccess, message, httpStatus, inputStream, null);
    }

    public HttpTaskResponse(boolean isSuccess, String message, int httpStatus, InputStream inputStream, String eTag) {
        this(isSuccess, message, httpStatus, inputStream, eTag, null);
    }

    public HttpTaskResponse(boolean isSuccess, String message, int httpStatus, InputStream inputStream, String eTag, String contentType) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.httpStatus = httpStatus;
        this.inputStream = inputStream;
        this.eTag = eTag;
        this.contentType = contentType;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
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
