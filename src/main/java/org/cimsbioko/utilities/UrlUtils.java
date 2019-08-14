package org.cimsbioko.utilities;

import android.content.Context;

import org.cimsbioko.R;
import static org.cimsbioko.utilities.MessageUtils.showLongToast;
import static org.cimsbioko.utilities.ConfigUtils.getPreferenceString;

public class UrlUtils {

    public static String buildServerUrl(Context context, String path) {
        String baseUrl = getPreferenceString(context, R.string.server_url_key, context.getString(R.string.default_server_url));
        if (baseUrl.trim().isEmpty()) {
            showLongToast(context, R.string.no_server_url);
            return null;
        }
        return baseUrl + path;
    }
}
