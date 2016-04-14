package org.openhds.mobile.utilities;

import android.content.Context;
import android.content.pm.PackageManager;

import org.openhds.mobile.R;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class ConfigUtils {

    public static String getResourceString(Context context, int id) {
        return context.getString(id);
    }

    public static String getPreferenceString(Context context, int key, String defaultValue) {
        return getDefaultSharedPreferences(context).getString(getResourceString(context, key), defaultValue);
    }

    public static String getPreferenceString(Context context, String key, String defaultValue) {
        return getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    public static String getVersion(Context ctx) throws PackageManager.NameNotFoundException {
        return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
    }

    public static String getAppName(Context context) {
        return getResourceString(context, R.string.app_name);
    }

    public static String getAppFullName(Context context) {
        String appName = getAppName(context);
        try {
            return String.format("%s %s", appName, getVersion(context));
        } catch (PackageManager.NameNotFoundException e) {
            return appName;
        }
    }
}
