package org.openhds.mobile.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import org.openhds.mobile.R;

public class ConfigUtils {

    public static final String FORM_ID = "form_id";

    public static String getResourceString(Context context, int id) {
        return (context.getString(id));
    }

    public static String getPreferenceString(Context context, int key, String defaultValue) {
        String keyString = getResourceString(context, key);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(keyString, defaultValue);
    }

    public static String getPreferenceString(Context context, String key, String defaultValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, defaultValue);
    }

    public static String getAppFullName(Context context) {
        String packageName = context.getPackageName();
        String appName = getResourceString(context, R.string.app_name);
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return appName + " (" +
                    packageInfo.versionName + " " +
                    packageInfo.versionCode + ")";

        } catch (PackageManager.NameNotFoundException e) {
            return appName;
        }
    }

    public static int getAppVersionNumber(Context context) {
        String packageName = context.getPackageName();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }
}
