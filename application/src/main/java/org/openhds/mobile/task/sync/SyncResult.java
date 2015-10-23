package org.openhds.mobile.task.sync;

/**
 * Represents the final outcome of a {@link SyncRequest} as executed by a
 * {@link SyncTask}.
 */
public class SyncResult {

    public enum Type {
        FULL, INCREMENTAL, NO_UPDATE, FAILURE
    }

    final Type type;
    final String message;
    final String eTag;

    SyncResult(Type type, String eTag) {
        this(type, null, eTag);
    }

    SyncResult(Type type, String message, String eTag) {
        this.type = type;
        this.message = message;
        this.eTag = eTag;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getETag() {
        return eTag;
    }
}