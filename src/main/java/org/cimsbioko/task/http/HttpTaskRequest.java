package org.cimsbioko.task.http;

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
    private final String auth;
    private final String accept;
    private final String eTag;
    private final File file;

    public HttpTaskRequest(String url, String accept, String auth) {
        this(url, accept, auth, null);
    }

    public HttpTaskRequest(String url, String accept, String auth, String eTag) {
        this(url, accept, auth, eTag, null);
    }

    public HttpTaskRequest(String url, String accept, String auth, String eTag, File saveTo) {
        this.url = url;
        this.auth = auth;
        this.accept = accept;
        this.eTag = eTag;
        file = saveTo;
    }

    public String getUrl() {
        return url;
    }

    public String getAccept() {
        return accept;
    }

    public String getAuth() {
        return auth;
    }

    public String getETag() {
        return eTag;
    }

    public File getFile() {
        return file;
    }
}
