package org.openhds.mobile.task.http;

import java.io.File;

/**
 * Url and credentials for an HTTP request.
 *
 * For now, just enough info to support GET requests.  This could
 * be extended, though, maybe with an optional output stream to
 * represent the request body.
 *
 * Pass an HttpTaskRequest to an HttpTask to make it go.
 *
 * BSH
 */
public class HttpTaskRequest {

    private final String url;
    private final String userName;
    private final String password;
    private final String accept;
    private final String eTag;
    private final File file;

    public HttpTaskRequest(String url, String accept, String userName, String password) {
        this(url, accept, userName, password, null);
    }

    public HttpTaskRequest(String url, String accept, String userName, String password, String eTag) {
        this(url, accept, userName, password, eTag, null);
    }

    public HttpTaskRequest(String url, String accept, String userName, String password, String eTag, File saveTo) {
        this.url = url;
        this.accept = accept;
        this.userName = userName;
        this.password = password;
        this.eTag = eTag;
        file = saveTo;
    }

    public String getUrl() {
        return url;
    }

    public String getAccept() {
        return accept;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getETag() {
        return eTag;
    }

    public File getFile() {
        return file;
    }
}
