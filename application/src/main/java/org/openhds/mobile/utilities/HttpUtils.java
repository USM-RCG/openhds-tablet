package org.openhds.mobile.utilities;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Constructs an {@link java.net.HttpURLConnection} with the given request
     * headers.
     */
    public static HttpURLConnection get(URL url, Map<String, String> headers) throws IOException {
        HttpURLConnection c = (HttpURLConnection)url.openConnection();
        for (Map.Entry<String, String> h : headers.entrySet()) {
            c.addRequestProperty(h.getKey(), h.getValue());
        }
        return c;
    }

    public static HttpURLConnection get(URL url, String accept, String auth, String eTag) throws IOException {
        Map<String, String> headers = new HashMap<>();
        if (accept != null) {
            headers.put("Accept", accept);
        }
        if (auth != null) {
            headers.put("Authorization", auth);
        }
        if (eTag != null) {
            headers.put("If-None-Match", eTag);
        }
        return get(url, headers);
    }
}
