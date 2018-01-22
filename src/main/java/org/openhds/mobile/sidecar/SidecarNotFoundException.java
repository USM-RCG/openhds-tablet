package org.openhds.mobile.sidecar;

public class SidecarNotFoundException extends Exception {

    public SidecarNotFoundException() {
    }

    public SidecarNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public SidecarNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
