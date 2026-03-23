package com.remote.app.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetSocketAddress
import java.net.Socket

data class DiscoveredTV(
    val name: String,
    val host: String,
    val port: Int,
    val lastSeenMillis: Long = 0L
)

class TVDiscoveryManager(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val serviceType = "_androidtvremote2._tcp."
    
    private val prefs = context.getSharedPreferences("TVSettings", Context.MODE_PRIVATE)

    private fun loadSavedTVs(): List<DiscoveredTV> {
        val saved = prefs.getStringSet("saved_tvs", emptySet()) ?: emptySet()
        return saved.mapNotNull { 
            val parts = it.split("||")
            if (parts.size >= 4) DiscoveredTV(parts[0], parts[1], parts[2].toInt(), parts[3].toLongOrNull() ?: 0L)
            else if (parts.size == 3) DiscoveredTV(parts[0], parts[1], parts[2].toInt(), 0L)
            else null
        }
    }

    private fun saveTV(tv: DiscoveredTV) {
        val saved = prefs.getStringSet("saved_tvs", emptySet())?.toMutableSet() ?: mutableSetOf()
        saved.removeAll { it.contains("||${tv.host}||") }
        saved.add("${tv.name}||${tv.host}||${tv.port}||${tv.lastSeenMillis}")
        prefs.edit().putStringSet("saved_tvs", saved).apply()
    }

    private val tvsMap = mutableMapOf<String, DiscoveredTV>()

    private val _discoveredTVs = MutableStateFlow<List<DiscoveredTV>>(emptyList())
    val discoveredTVs: StateFlow<List<DiscoveredTV>> = _discoveredTVs
    
    init {
        loadSavedTVs().forEach { tvsMap[it.host] = it }
        _discoveredTVs.value = tvsMap.values.sortedBy { it.name }
    }

    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private fun updateDiscoveredTvs() {
        _discoveredTVs.value = tvsMap.values.sortedBy { it.name }
    }

    private fun createListener() = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d("TVDiscovery", "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            if (service.serviceType == serviceType) {
                try {
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.e("TVDiscovery", "Resolve failed: $errorCode")
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            val host = serviceInfo.host.hostAddress ?: return
                            val tv = DiscoveredTV(
                                name = serviceInfo.serviceName,
                                host = host,
                                port = serviceInfo.port,
                                lastSeenMillis = System.currentTimeMillis()
                            )
                            tvsMap[host] = tv
                            saveTV(tv)
                            updateDiscoveredTvs()
                        }
                    })
                } catch (e: Exception) {
                    Log.e("TVDiscovery", "Error resolving service", e)
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // Unchanged: we keep it in memory so the timestamp reflects the last time it was seen
        }

        override fun onDiscoveryStopped(serviceType: String) {
            updateDiscoveredTvs()
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            try { nsdManager.stopServiceDiscovery(this) } catch (e: Exception) {}
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            try { nsdManager.stopServiceDiscovery(this) } catch (e: Exception) {}
        }
    }

    private fun pingSavedDevices() {
        CoroutineScope(Dispatchers.IO).launch {
            tvsMap.values.toList().forEach { tv ->
                launch {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(tv.host, tv.port), 1500) // 1.5 seconds timeout
                        socket.close()
                        
                        // If we reach here, port is open! Update lastSeenMillis
                        val updatedTv = tv.copy(lastSeenMillis = System.currentTimeMillis())
                        tvsMap[tv.host] = updatedTv
                        saveTV(updatedTv)
                        updateDiscoveredTvs()
                    } catch (e: Exception) {
                        // Port is closed or host is unreachable, do nothing.
                    }
                }
            }
        }
    }

    fun startDiscovery() {
        stopDiscovery()
        updateDiscoveredTvs()
        pingSavedDevices()
        discoveryListener = createListener()
        try {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e("TVDiscovery", "Failed to start discovery", e)
        }
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                 Log.e("TVDiscovery", "Failed to stop discovery", e)
            }
            discoveryListener = null
        }
    }
}
