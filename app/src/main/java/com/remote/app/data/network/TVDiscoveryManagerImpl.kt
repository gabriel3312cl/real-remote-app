package com.remote.app.data.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.remote.app.BuildConfig
import com.remote.app.domain.AppConstants
import com.remote.app.domain.PrefsKeys
import com.remote.app.domain.model.DiscoveredTV
import com.remote.app.domain.repository.TVDiscoveryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TVDiscoveryManagerImpl @Inject constructor(
    context: Context
) : TVDiscoveryRepository {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val serviceType = "_androidtvremote2._tcp."
    private val prefs = context.getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)

    private val tvsMap = mutableMapOf<String, DiscoveredTV>()

    private val _discoveredTVs = MutableStateFlow<List<DiscoveredTV>>(emptyList())
    override val discoveredTVs: StateFlow<List<DiscoveredTV>> = _discoveredTVs

    private var discoveryListener: NsdManager.DiscoveryListener? = null

    init {
        loadSavedTVs().forEach { tvsMap[it.host] = it }
        _discoveredTVs.value = tvsMap.values.sortedBy { it.name }
    }

    private fun loadSavedTVs(): List<DiscoveredTV> {
        val saved = prefs.getStringSet(PrefsKeys.KEY_SAVED_TVS, emptySet()) ?: emptySet()
        return saved.mapNotNull {
            val parts = it.split(AppConstants.TV_DATA_SEPARATOR)
            if (parts.size >= 4) DiscoveredTV(parts[0], parts[1], parts[2].toInt(), parts[3].toLongOrNull() ?: 0L)
            else if (parts.size == 3) DiscoveredTV(parts[0], parts[1], parts[2].toInt(), 0L)
            else null
        }
    }

    private fun saveTV(tv: DiscoveredTV) {
        val sep = AppConstants.TV_DATA_SEPARATOR
        val saved = prefs.getStringSet(PrefsKeys.KEY_SAVED_TVS, emptySet())?.toMutableSet() ?: mutableSetOf()
        saved.removeAll { it.contains("${sep}${tv.host}${sep}") }
        saved.add("${tv.name}${sep}${tv.host}${sep}${tv.port}${sep}${tv.lastSeenMillis}")
        prefs.edit().putStringSet(PrefsKeys.KEY_SAVED_TVS, saved).apply()
    }

    private fun updateDiscoveredTvs() {
        _discoveredTVs.value = tvsMap.values.sortedBy { it.name }
    }

    private fun createListener() = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            if (service.serviceType == serviceType) {
                try {
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            if (BuildConfig.DEBUG) Log.e(TAG, "Resolve failed: $errorCode")
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
                    if (BuildConfig.DEBUG) Log.e(TAG, "Error resolving service", e)
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {}
        override fun onDiscoveryStopped(serviceType: String) {
            updateDiscoveredTvs()
        }
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            try { nsdManager.stopServiceDiscovery(this) } catch (_: Exception) {}
        }
        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            try { nsdManager.stopServiceDiscovery(this) } catch (_: Exception) {}
        }
    }

    private fun pingSavedDevices() {
        CoroutineScope(Dispatchers.IO).launch {
            tvsMap.values.toList().forEach { tv ->
                launch {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(tv.host, tv.port), AppConstants.PING_TIMEOUT_MS)
                        socket.close()
                        val updatedTv = tv.copy(lastSeenMillis = System.currentTimeMillis())
                        tvsMap[tv.host] = updatedTv
                        saveTV(updatedTv)
                        updateDiscoveredTvs()
                    } catch (_: Exception) { /* host unreachable */ }
                }
            }
        }
    }

    override fun startDiscovery() {
        stopDiscovery()
        updateDiscoveredTvs()
        pingSavedDevices()
        discoveryListener = createListener()
        try {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Failed to start discovery", e)
        }
    }

    override fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Failed to stop discovery", e)
            }
            discoveryListener = null
        }
    }

    companion object {
        private const val TAG = "TVDiscovery"
    }
}
