package org.cimsbioko.sidecar;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class ResolveListener implements NsdManager.ResolveListener {

    private static final String TAG = ResolveListener.class.getSimpleName();

    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
        Log.d(TAG, "resolve failed: serviceInfo=" + serviceInfo + " error code: " + errorCode);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
        Log.d(TAG, "service resolved: serviceInfo=" + serviceInfo);
    }
}
