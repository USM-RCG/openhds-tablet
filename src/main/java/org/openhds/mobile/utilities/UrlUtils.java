package org.openhds.mobile.utilities;

import android.content.Context;

import org.openhds.mobile.R;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;

public class UrlUtils {

    public static String buildServerUrl(Context context, String path) {
        String openHdsBaseUrl = getPreferenceString(context, R.string.openhds_server_url_key, context.getString(R.string.default_openhds_server_url));
        if (openHdsBaseUrl.trim().isEmpty()) {
            showLongToast(context, R.string.no_server_url);
            return null;
        }
        return openHdsBaseUrl + path;
    }
}
