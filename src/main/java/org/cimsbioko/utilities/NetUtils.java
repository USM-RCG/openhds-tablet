package org.cimsbioko.utilities;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.cimsbioko.App;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class NetUtils {

    private static ConnectivityManager getConnMan() {
        return (ConnectivityManager) App.getApp().getSystemService(CONNECTIVITY_SERVICE);
    }

    public static boolean isConnected() {
        ConnectivityManager cm = getConnMan();
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }
        return activeNetwork.isConnected();
    }

    public static boolean isWiFiConnected() {
        ConnectivityManager cm = getConnMan();
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }
        return activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
