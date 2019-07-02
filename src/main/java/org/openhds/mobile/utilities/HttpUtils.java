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
     * <p>
     * FIXME: password exposed to memory scan as String via constant pool
     */
    public static String encodeBasicCreds(String username, String password) {
        return String.format("Basic %s",
                encodeToString((username + ":" + password).getBytes(), DEFAULT)
        );
    }

    /**
     * Encodes an HTTP Bearer authentication header for the specified token.
     * <p>
     * FIXME: password exposed to memory scan as String via constant pool
     */
    public static String encodeBearerCreds(String token) {
        return String.format("Bearer %s", token);
    }

    /**
     * Constructs an {@link HttpURLConnection} with the given request headers.
     */
    public static HttpURLConnection get(URL url, Map<String, String> headers) throws IOException {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        for (Map.Entry<String, String> h : headers.entrySet()) {
            c.addRequestProperty(h.getKey(), h.getValue());
        }
        return c;
    }

    /**
     * Constructs an {@link HttpURLConnection} with the specified settings, if non-null.
     */
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
