package org.openhds.mobile.task.http;

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
    private final int titleId;
    private final String url;
    private final String userName;
    private final String password;
    private final String accept;
    private final String eTag;

    public HttpTaskRequest(int titleId, String url, String accept, String userName, String password) {
        this(titleId, url, accept, userName, password, null);
    }

    public HttpTaskRequest(int titleId, String url, String accept, String userName, String password, String eTag) {
        this.titleId = titleId;
        this.url = url;
        this.accept = accept;
        this.userName = userName;
        this.password = password;
        this.eTag = eTag;
    }

    public int getTitleId() {
        return titleId;
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
}
