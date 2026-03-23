package com.remote.app

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kunal52.AndroidRemoteContext
import com.kunal52.AndroidRemoteTv
import com.kunal52.AndroidTvListener
import com.kunal52.remote.Remotemessage
import com.remote.app.network.DiscoveredTV
import com.remote.app.network.TVDiscoveryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ConnectionState {
    DISCONNECTED, PAIRING_PIN_REQUESTED, CONNECTING, CONNECTED, ERROR
}

class RemoteViewModel(application: Application) : AndroidViewModel(application) {
    private val tvDiscoveryManager = TVDiscoveryManager(application)
    
    val discoveredTVs = tvDiscoveryManager.discoveredTVs
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var androidRemoteTv: AndroidRemoteTv? = null
    private var connectedTv: DiscoveredTV? = null

    init {
        // Initialize keystore path properly for Android
        AndroidRemoteContext.getInstance().initialize(application)
        AndroidRemoteContext.getInstance().clientName = "Minimal TV Remote"
    }

    fun startDiscovery() {
        _connectionState.value = ConnectionState.DISCONNECTED
        viewModelScope.launch {
            if (_isScanning.value) return@launch
            _isScanning.value = true
            tvDiscoveryManager.startDiscovery()
            kotlinx.coroutines.delay(5000)
            tvDiscoveryManager.stopDiscovery()
            _isScanning.value = false
        }
    }

    fun connectToTv(tv: DiscoveredTV) {
        connectedTv = tv
        tvDiscoveryManager.stopDiscovery()
        _connectionState.value = ConnectionState.CONNECTING
        
        androidRemoteTv = AndroidRemoteTv()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                androidRemoteTv?.connect(tv.host, object : AndroidTvListener {
                    override fun onSessionCreated() {}

                    override fun onSecretRequested() {
                        _connectionState.value = ConnectionState.PAIRING_PIN_REQUESTED
                    }

                    override fun onPaired() {
                        Log.d("RemoteViewModel", "Paired successfully")
                    }

                    override fun onConnectingToRemote() {
                        _connectionState.value = ConnectionState.CONNECTING
                    }

                    override fun onConnected() {
                        _connectionState.value = ConnectionState.CONNECTED
                    }

                    override fun onDisconnect() {
                        _connectionState.value = ConnectionState.DISCONNECTED
                    }

                    override fun onError(error: String?) {
                        _errorMessage.value = error
                        _connectionState.value = ConnectionState.ERROR
                    }
                })
            } catch (e: Exception) {
                Log.e("RemoteViewModel", "Connect error", e)
                _errorMessage.value = e.message
                _connectionState.value = ConnectionState.ERROR
            }
        }
    }

    fun providePairingPin(pin: String) {
        _connectionState.value = ConnectionState.CONNECTING
        viewModelScope.launch(Dispatchers.IO) {
            androidRemoteTv?.sendSecret(pin)
        }
    }

    fun sendCommand(keyCode: Remotemessage.RemoteKeyCode) {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            viewModelScope.launch(Dispatchers.IO) {
                androidRemoteTv?.sendCommand(keyCode, Remotemessage.RemoteDirection.SHORT)
            }
        }
    }
    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            androidRemoteTv?.disconnect()
        }
        connectedTv = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun checkConnectionAndReconnect() {
        if (_connectionState.value == ConnectionState.ERROR || _connectionState.value == ConnectionState.DISCONNECTED) {
            connectedTv?.let { tv ->
                connectToTv(tv)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        tvDiscoveryManager.stopDiscovery()
    }
}
