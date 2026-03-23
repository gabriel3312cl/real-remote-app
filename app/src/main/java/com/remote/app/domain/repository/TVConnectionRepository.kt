package com.remote.app.domain.repository

import com.remote.app.domain.model.ConnectionState
import com.remote.app.domain.model.DiscoveredTV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface TVConnectionRepository {
    val connectionState: StateFlow<ConnectionState>
    val errorMessage: StateFlow<String?>

    fun connectToTv(tv: DiscoveredTV, scope: CoroutineScope)
    fun sendSecret(pin: String, scope: CoroutineScope)
    fun sendCommand(keyCode: Int, scope: CoroutineScope)
    fun disconnect(scope: CoroutineScope)
    fun resetState()
    fun checkConnectionAndReconnect(scope: CoroutineScope)
}
