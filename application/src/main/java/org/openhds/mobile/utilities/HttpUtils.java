package org.openhds.mobile.utilities;

import static android.util.Base64.DEFAULT;
import static android.util.Base64.encodeToString;

public class HttpUtils {

    /**
     * Encodes an HTTP Basic authentication header for the specified username
     * and password.
     */
    public static String basicAuth(String username, String password) {
        // FIXME: password exposed to memory scan as String via constant pool
        return String.format("Basic %s",
                encodeToString((username + ":" + password).getBytes(), DEFAULT)
        );
    }
}
