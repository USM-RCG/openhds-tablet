package org.cimsbioko.syncadpt;

import android.content.Context;
import android.os.Build;
import org.json.JSONException;
import org.json.JSONObject;
import org.cimsbioko.R;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.os.Build.*;
import static android.os.Build.VERSION.RELEASE;
import static java.util.Arrays.asList;
import static org.cimsbioko.utilities.HttpUtils.encodeBasicCreds;
import static org.cimsbioko.utilities.IOUtils.close;
import static org.cimsbioko.utilities.IOUtils.copy;
import static org.cimsbioko.utilities.UrlUtils.buildServerUrl;

public class AuthUtils {

    /**
     * Calls server web api to register the device (initial association).
     *
     * @param ctx      used to get the registration endpoint relative to server url in settings
     * @param name     the device 'username' to use when authenticating to device api
     * @param secret the secret to use when authenticating
     * @return a {@link JSONObject} containing the response data.
     * @throws IOException   when url is bad, or io fails
     * @throws JSONException when construction of the response object fails
     */
    public static JSONObject register(Context ctx, String name, String secret) throws IOException, JSONException {
        HttpURLConnection urlConn = (HttpURLConnection) getRegistrationEndpoint(ctx).openConnection();
        urlConn.setRequestProperty("Content-Type", "application/json");
        urlConn.setRequestProperty("Authorization", encodeBasicCreds(name, secret));
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            urlConn.setDoOutput(true);
            urlConn.setChunkedStreamingMode(0);
            out = new BufferedOutputStream(urlConn.getOutputStream());
            out.write(getDeviceDescription().getBytes());
            out.flush();
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK || !urlConn.getContentType().startsWith("application/json")) {
                throw new RuntimeException(String.format(
                        "unexpected response: status %s, mime type = %s",
                        urlConn.getResponseCode(), urlConn.getContentType()));
            } else {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                in = new BufferedInputStream(urlConn.getInputStream());
                copy(in, bout);
                return new JSONObject(bout.toString());
            }
        } finally {
            close(out, in);
            urlConn.disconnect();
        }
    }

    /**
     * Calls server web api to obtain a new access token for the device, in other words - a refreshed token.
     *
     * @param ctx      used to get the token endpoint relative to server url in settings
     * @param name     the device 'username' to use when authenticating to device api
     * @param secret the secret to use when authenticating
     * @return a {@link JSONObject} containing the response data.
     * @throws IOException   when url is bad, or io fails
     * @throws JSONException when construction of the response object fails
     */
    public static JSONObject token(Context ctx, String name, String secret) throws IOException, JSONException {
        HttpURLConnection urlConn = (HttpURLConnection) getTokenEndpoint(ctx).openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setRequestProperty("Content-Type", "application/json");
        urlConn.setRequestProperty("Authorization", encodeBasicCreds(name, secret));
        BufferedInputStream in = null;
        try {
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK || !urlConn.getContentType().startsWith("application/json")) {
                throw new RuntimeException(String.format(
                        "unexpected response: status %s, mime type = %s",
                        urlConn.getResponseCode(), urlConn.getContentType()));
            } else {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                in = new BufferedInputStream(urlConn.getInputStream());
                copy(in, bout);
                return new JSONObject(bout.toString());
            }
        } finally {
            close(in);
            urlConn.disconnect();
        }
    }

    /**
     * Returns a device description suitable for general differentiation of device types. This is used to help device
     * administrators identify and manage registered devices with more information than just the device name.
     *
     * @return a description including manufacturer, brand, model, and Android release.
     * @throws JSONException
     */
    public static String getDeviceDescription() throws JSONException {
        JSONObject json = new JSONObject();
        StringBuilder b = new StringBuilder();
        for (String s : asList(MANUFACTURER, BRAND, Build.MODEL, String.format("Android %s", RELEASE))) {
            if (!UNKNOWN.equals(s)) {
                b.append(s);
                b.append("\n");
            }
        }
        return json.put("description", b.toString()).toString();
    }

    /**
     * Returns the server {@link URL} to use to register as a device. It is constructed based on the application's
     * configured server endpoint.
     *
     * @param ctx application context to use for relevant config values
     * @return a {@link URL} object corresponding to the device registration endpoint
     * @throws MalformedURLException when the constructed value is not a valid URL
     */
    public static URL getRegistrationEndpoint(Context ctx) throws MalformedURLException {
        return new URL(buildServerUrl(ctx, ctx.getString(R.string.auth_registration_path)));
    }

    /**
     * Returns the server {@link URL} to use to issue access tokens as a device. It is constructed relative to the
     * application's configured server endpoint.
     *
     * @param ctx application context to use for relevant config values
     * @return a {@link URL} object corresponding to the device token endpoint
     * @throws MalformedURLException when the constructed value is not a valid URL
     */
    public static URL getTokenEndpoint(Context ctx) throws MalformedURLException {
        return new URL(buildServerUrl(ctx, ctx.getString(R.string.auth_token_path)));
    }
}