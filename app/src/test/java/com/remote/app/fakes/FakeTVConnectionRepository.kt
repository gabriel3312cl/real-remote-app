package com.remote.app.fakes

import com.remote.app.domain.model.ConnectionState
import com.remote.app.domain.model.DiscoveredTV
import com.remote.app.domain.repository.TVConnectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import com.remote.app.domain.model.ConnectionStep

class FakeTVConnectionRepository : TVConnectionRepository {

    val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    val _connectionStep = MutableStateFlow<ConnectionStep>(ConnectionStep.Idle)
    override val connectionStep: StateFlow<ConnectionStep> = _connectionStep

    val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage

    val _latencyMs = MutableStateFlow<Long?>(null)
    override val latencyMs: StateFlow<Long?> = _latencyMs

    var connectToTvCalled = false
    var lastConnectedTv: DiscoveredTV? = null
    var sendSecretCalled = false
    var lastSecret: String? = null
    var sendCommandCalled = false
    var lastKeyCode: Int? = null
    var disconnectCalled = false
    var resetStateCalled = false
    var checkReconnectCalled = false

    override fun connectToTv(tv: DiscoveredTV, scope: CoroutineScope) {
        connectToTvCalled = true
        lastConnectedTv = tv
    }

    override fun sendSecret(pin: String, scope: CoroutineScope) {
        sendSecretCalled = true
        lastSecret = pin
    }

    override fun sendCommand(keyCode: Int, scope: CoroutineScope) {
        sendCommandCalled = true
        lastKeyCode = keyCode
    }

    override fun disconnect(scope: CoroutineScope) {
        disconnectCalled = true
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override fun resetState() {
        resetStateCalled = true
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override fun checkConnectionAndReconnect(scope: CoroutineScope) {
        checkReconnectCalled = true
    }
}
