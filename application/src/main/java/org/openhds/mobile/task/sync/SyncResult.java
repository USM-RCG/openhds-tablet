package org.openhds.mobile.task.sync;

/**
 * Represents the final outcome of a {@link SyncRequest} as executed by a
 * {@link SyncTask}.
 */
public class SyncResult {

    public enum Type {

        FULL("Full sync successful"),
        INCREMENTAL("Incremental sync successful"),
        NO_UPDATE("Content not modified"),
        FAILURE("Sync failed");

        private String message;

        Type(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
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

    @Override
    public String toString() {
        switch (type) {
            case FULL:
            case INCREMENTAL:
            case NO_UPDATE:
                return type.toString();
            default:
                return type.toString() + (message != null ? " " + message : "");
        }
    }
}