package org.openhds.mobile.sidecar;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Sidecar {

    private static final String SERVICE_TYPE = "_cimssc._tcp";

    public static NsdServiceInfo discover(final NsdManager manager, int waitSeconds) throws SidecarNotFoundException {

        final BlockingQueue<NsdServiceInfo> resolvedServices = new ArrayBlockingQueue<>(25);

        DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void onServiceFound(NsdServiceInfo unresolvedInfo) {
                super.onServiceFound(unresolvedInfo);
                manager.resolveService(unresolvedInfo, new ResolveListener() {
                    @Override
                    public void onServiceResolved(NsdServiceInfo resolvedInfo) {
                        super.onServiceResolved(resolvedInfo);
                        resolvedServices.add(resolvedInfo);
                    }
                });
            }
        };

        manager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener);
        try {
            NsdServiceInfo info = resolvedServices.poll(waitSeconds, TimeUnit.SECONDS);
            if (info == null) {
                throw new SidecarNotFoundException("sidecar not found");
            }
            return info;
        } catch (InterruptedException e) {
            throw new SidecarNotFoundException("sidecar not found, interrupted during discovery", e);
        } finally {
            manager.stopServiceDiscovery(listener);
        }
    }
}
