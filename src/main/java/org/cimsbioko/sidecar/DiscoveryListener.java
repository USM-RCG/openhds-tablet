package org.cimsbioko.sidecar;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class DiscoveryListener implements NsdManager.DiscoveryListener {

    private static final String TAG = DiscoveryListener.class.getSimpleName();

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.i(TAG, "start discovery failed: serviceType=" + serviceType + ", error code=" + errorCode);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.i(TAG, "stop discovery failed: serviceType=" + serviceType + ", error code=" + errorCode);
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.i(TAG, "discovery started: serviceType=" + serviceType);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.i(TAG, "discovery stopped: serviceType=" + serviceType);
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        Log.i(TAG, "service found: serviceInfo=" + serviceInfo);
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        Log.i(TAG, "service lost: serviceInfo=" + serviceInfo);
    }
}
