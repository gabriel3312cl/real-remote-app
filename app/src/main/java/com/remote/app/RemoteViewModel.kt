package com.remote.app

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunal52.remote.Remotemessage
import com.remote.app.billing.BillingManager
import com.remote.app.connection.ConnectionState
import com.remote.app.connection.TVConnectionManager
import com.remote.app.i18n.AppLanguage
import com.remote.app.network.DiscoveredTV
import com.remote.app.network.TVDiscoveryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteViewModel @Inject constructor(
    private val prefs: SharedPreferences,
    val billingManager: BillingManager,
    private val tvDiscoveryManager: TVDiscoveryManager,
    private val tvConnectionManager: TVConnectionManager
) : ViewModel() {

    // --- Language ---
    private val _appLanguage = MutableStateFlow(
        AppLanguage.valueOf(prefs.getString("selected_language", "SYSTEM") ?: "SYSTEM")
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString("selected_language", language.name).apply()
        _appLanguage.value = language
    }

    // --- Pro status (delegated to BillingManager) ---
    val isPro = billingManager.isPro

    // --- Discovery ---
    val discoveredTVs = tvDiscoveryManager.discoveredTVs

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    fun startDiscovery() {
        tvConnectionManager.resetState()
        viewModelScope.launch {
            if (_isScanning.value) return@launch
            _isScanning.value = true
            tvDiscoveryManager.startDiscovery()
            kotlinx.coroutines.delay(5000)
            tvDiscoveryManager.stopDiscovery()
            _isScanning.value = false
        }
    }

    // --- Connection (delegated to TVConnectionManager) ---
    val connectionState: StateFlow<ConnectionState> = tvConnectionManager.connectionState
    val errorMessage: StateFlow<String?> = tvConnectionManager.errorMessage

    fun connectToTv(tv: DiscoveredTV) {
        tvDiscoveryManager.stopDiscovery()
        tvConnectionManager.connectToTv(tv, viewModelScope)
    }

    fun providePairingPin(pin: String) {
        tvConnectionManager.sendSecret(pin, viewModelScope)
    }

    fun sendCommand(keyCode: Remotemessage.RemoteKeyCode) {
        tvConnectionManager.sendCommand(keyCode, viewModelScope)
    }

    fun disconnect() {
        tvConnectionManager.disconnect(viewModelScope)
    }

    fun checkConnectionAndReconnect() {
        tvConnectionManager.checkConnectionAndReconnect(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        tvDiscoveryManager.stopDiscovery()
    }
}
