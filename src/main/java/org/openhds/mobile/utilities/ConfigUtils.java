package org.openhds.mobile.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import org.openhds.mobile.R;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.NavigatorModule;
import org.openhds.mobile.navconfig.forms.Binding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ConfigUtils {

    public static String getResourceString(Context context, int id) {
        return context.getString(id);
    }

    public static SharedPreferences getSharedPrefs(Context ctx) {
        // multi-process needed for sync adapter to see preference changes
        return ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
    }

    public static String getPreferenceString(Context context, int key, String defaultValue) {
        return getSharedPrefs(context).getString(getResourceString(context, key), defaultValue);
    }

    public static String getPreferenceString(Context context, String key, String defaultValue) {
        return getSharedPrefs(context).getString(key, defaultValue);
    }

    public static Set<String> getMultiSelectPreference(Context context, String key, Set<String> defaultValues) {
        return getSharedPrefs(context).getStringSet(key, defaultValues);
    }

    public static Boolean getPreferenceBool(Context context, String key, Boolean defaultValue) {
        return getSharedPrefs(context).getBoolean(key, defaultValue);
    }

    public static String getVersion(Context ctx) throws PackageManager.NameNotFoundException {
        return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
    }

    public static Collection<NavigatorModule> getActiveModules(Context ctx) {
        NavigatorConfig cfg = NavigatorConfig.getInstance();
        List<NavigatorModule> actives = new ArrayList<>();
        Set<String> activeModuleNames = getMultiSelectPreference(ctx,
                ctx.getString(R.string.active_modules_key), cfg.getModuleNames());
        for (NavigatorModule module : cfg.getModules()) {
            if (activeModuleNames.contains(module.getName())) {
                actives.add(module);
            }
        }
        return actives;
    }

    public static Collection<NavigatorModule> getActiveModuleForBinding(Context ctx, Binding binding) {
        List<NavigatorModule> modules = new ArrayList<>();
        for (NavigatorModule module : getActiveModules(ctx)) {
            if (module.getBindings().containsKey(binding.getName())) {
                modules.add(module);
            }
        }
        return modules;
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
