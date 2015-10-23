package org.openhds.mobile.task.sync;

import java.io.File;
import java.net.URL;

/**
 * Encapsulates everything necessary to perform a synchronization with
 * {@link SyncTask}.
 */
public class SyncRequest {

    final URL endpoint;
    final String user;
    final String pass;
    final File basis;
    final File target;
    final String mimeType;
    final String eTag;

    public SyncRequest(URL endpoint, String user, String pass,
                       File basis, File target, String mimeType, String eTag) {
        this.user = user;
        this.pass = pass;
        this.endpoint = endpoint;
        this.basis = basis;
        this.target = target;
        this.mimeType = mimeType;
        this.eTag = eTag;
    }
}