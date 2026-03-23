package com.remote.app.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DiscoveredTV(
    val name: String,
    val host: String,
    val port: Int
)

class TVDiscoveryManager(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val serviceType = "_androidtvremote2._tcp."
    
    private val _discoveredTVs = MutableStateFlow<List<DiscoveredTV>>(emptyList())
    val discoveredTVs: StateFlow<List<DiscoveredTV>> = _discoveredTVs
    
    private val discoveredSet = mutableSetOf<DiscoveredTV>()

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d("TVDiscovery", "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d("TVDiscovery", "Service found: ${service.serviceName}")
            if (service.serviceType == serviceType) {
                try {
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.e("TVDiscovery", "Resolve failed: $errorCode")
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            Log.d("TVDiscovery", "Resolve Succeeded. ${serviceInfo.serviceName}")
                            val tv = DiscoveredTV(
                                name = serviceInfo.serviceName,
                                host = serviceInfo.host.hostAddress ?: return,
                                port = serviceInfo.port
                            )
                            discoveredSet.add(tv)
                            _discoveredTVs.value = discoveredSet.toList()
                        }
                    })
                } catch (e: Exception) {
                    Log.e("TVDiscovery", "Error resolving service", e)
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e("TVDiscovery", "Service lost: ${service.serviceName}")
            val iterator = discoveredSet.iterator()
            var changed = false
            while (iterator.hasNext()) {
                if (iterator.next().name == service.serviceName) {
                    iterator.remove()
                    changed = true
                }
            }
            if (changed) {
                _discoveredTVs.value = discoveredSet.toList()
            }
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i("TVDiscovery", "Discovery stopped: $serviceType")
            discoveredSet.clear()
            _discoveredTVs.value = emptyList()
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("TVDiscovery", "Discovery failed: Error code:$errorCode")
            try {
                nsdManager.stopServiceDiscovery(this)
            } catch (e: Exception) {}
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("TVDiscovery", "Discovery failed: Error code:$errorCode")
            try {
                nsdManager.stopServiceDiscovery(this)
            } catch (e: Exception) {}
        }
    }

    fun startDiscovery() {
        discoveredSet.clear()
        _discoveredTVs.value = emptyList()
        try {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e("TVDiscovery", "Failed to start discovery", e)
        }
    }

    fun stopDiscovery() {
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
             Log.e("TVDiscovery", "Failed to stop discovery", e)
        }
    }
}
