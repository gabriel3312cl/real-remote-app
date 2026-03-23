package com.remote.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunal52.AndroidRemoteContext
import com.kunal52.AndroidRemoteTv
import com.kunal52.AndroidTvListener
import com.kunal52.remote.Remotemessage
import com.remote.app.network.DiscoveredTV
import com.remote.app.network.TVDiscoveryManager
import com.remote.app.i18n.AppLanguage
import com.remote.app.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ConnectionState {
    DISCONNECTED, PAIRING_PIN_REQUESTED, CONNECTING, CONNECTED, ERROR
}

@HiltViewModel
class RemoteViewModel @Inject constructor(
    private val prefs: SharedPreferences,
    val billingManager: BillingManager,
    private val tvDiscoveryManager: TVDiscoveryManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _appLanguage = MutableStateFlow(
        AppLanguage.valueOf(prefs.getString("selected_language", "SYSTEM") ?: "SYSTEM")
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString("selected_language", language.name).apply()
        _appLanguage.value = language
    }

    val isPro = billingManager.isPro
    
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
        AndroidRemoteContext.getInstance().initialize(context)
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
                try {
                    androidRemoteTv?.sendCommand(keyCode, Remotemessage.RemoteDirection.SHORT)
                } catch (e: Exception) {
                    Log.e("RemoteViewModel", "sendCommand error (Broken pipe / TV slept)", e)
                    disconnect()
                }
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
