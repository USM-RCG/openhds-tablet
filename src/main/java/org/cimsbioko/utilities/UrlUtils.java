package org.cimsbioko.utilities;

import android.content.Context;

import org.cimsbioko.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.cimsbioko.utilities.MessageUtils.showLongToast;
import static org.cimsbioko.utilities.ConfigUtils.getPreferenceString;

public class UrlUtils {

    public static String buildServerUrl(Context context, String path) {
        String baseUrl = getServerBaseUrl(context);
        if (baseUrl.trim().isEmpty()) {
            showLongToast(context, R.string.no_server_url);
            return null;
        }
        return baseUrl + path;
    }

    private static String getServerBaseUrl(Context context) {
        return getPreferenceString(context, R.string.server_url_key, context.getString(R.string.default_server_url));
    }

    public static void setServerUrl(Context context, String url) {
        ConfigUtils.getSharedPrefs(context)
                .edit()
                .putString(context.getString(R.string.server_url_key), url)
                .apply();
    }

    public static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
