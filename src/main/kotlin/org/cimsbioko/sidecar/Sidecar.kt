package org.cimsbioko.sidecar

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

object Sidecar {

    private const val SERVICE_TYPE = "_cimssc._tcp"

    @Throws(SidecarNotFoundException::class)
    fun discover(manager: NsdManager, waitSeconds: Int): NsdServiceInfo {
        val resolvedServices: BlockingQueue<NsdServiceInfo> = ArrayBlockingQueue(25)
        val listener: DiscoveryListener = object : DiscoveryListener() {
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                super.onServiceFound(serviceInfo)
                manager.resolveService(serviceInfo, object : ResolveListener() {
                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        super.onServiceResolved(serviceInfo)
                        resolvedServices.add(serviceInfo)
                    }
                })
            }
        }
        manager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        return try {
            resolvedServices.poll(waitSeconds.toLong(), TimeUnit.SECONDS)
                    ?: throw SidecarNotFoundException("sidecar not found")
        } catch (e: InterruptedException) {
            throw SidecarNotFoundException("sidecar not found, interrupted during discovery", e)
        } finally {
            manager.stopServiceDiscovery(listener)
        }
    }
}


open class DiscoveryListener : NsdManager.DiscoveryListener {

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.i(TAG, "start discovery failed: serviceType=$serviceType, error code=$errorCode")
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.i(TAG, "stop discovery failed: serviceType=$serviceType, error code=$errorCode")
    }

    override fun onDiscoveryStarted(serviceType: String) {
        Log.i(TAG, "discovery started: serviceType=$serviceType")
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Log.i(TAG, "discovery stopped: serviceType=$serviceType")
    }

    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        Log.i(TAG, "service found: serviceInfo=$serviceInfo")
    }

    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
        Log.i(TAG, "service lost: serviceInfo=$serviceInfo")
    }

    companion object {
        private val TAG = DiscoveryListener::class.java.simpleName
    }
}


open class ResolveListener : NsdManager.ResolveListener {

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        Log.d(TAG, "resolve failed: serviceInfo=$serviceInfo error code: $errorCode")
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        Log.d(TAG, "service resolved: serviceInfo=$serviceInfo")
    }

    companion object {
        private val TAG = ResolveListener::class.java.simpleName
    }
}


class SidecarNotFoundException : Exception {
    constructor(detailMessage: String?) : super(detailMessage)
    constructor(detailMessage: String?, throwable: Throwable?) : super(detailMessage, throwable)
}