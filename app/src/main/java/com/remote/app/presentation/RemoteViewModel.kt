package com.remote.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunal52.remote.Remotemessage
import com.remote.app.domain.model.ConnectionState
import com.remote.app.domain.model.DiscoveredTV
import com.remote.app.domain.repository.BillingRepository
import com.remote.app.domain.repository.SettingsRepository
import com.remote.app.domain.repository.TVConnectionRepository
import com.remote.app.domain.repository.TVDiscoveryRepository
import com.remote.app.i18n.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    val billingRepository: BillingRepository,
    private val tvDiscoveryRepository: TVDiscoveryRepository,
    private val tvConnectionRepository: TVConnectionRepository
) : ViewModel() {

    // --- Language ---
    private val _appLanguage = MutableStateFlow(settingsRepository.getLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    fun setAppLanguage(language: AppLanguage) {
        settingsRepository.setLanguage(language)
        _appLanguage.value = language
    }

    // --- Pro status ---
    val isPro = billingRepository.isPro

    // --- Discovery ---
    val discoveredTVs = tvDiscoveryRepository.discoveredTVs

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    fun startDiscovery() {
        tvConnectionRepository.resetState()
        viewModelScope.launch {
            if (_isScanning.value) return@launch
            _isScanning.value = true
            tvDiscoveryRepository.startDiscovery()
            kotlinx.coroutines.delay(5000)
            tvDiscoveryRepository.stopDiscovery()
            _isScanning.value = false
        }
    }

    // --- Connection ---
    val connectionState: StateFlow<ConnectionState> = tvConnectionRepository.connectionState
    val errorMessage: StateFlow<String?> = tvConnectionRepository.errorMessage

    fun connectToTv(tv: DiscoveredTV) {
        tvDiscoveryRepository.stopDiscovery()
        tvConnectionRepository.connectToTv(tv, viewModelScope)
    }

    fun providePairingPin(pin: String) {
        tvConnectionRepository.sendSecret(pin, viewModelScope)
    }

    fun sendCommand(keyCode: Remotemessage.RemoteKeyCode) {
        tvConnectionRepository.sendCommand(keyCode.number, viewModelScope)
    }

    fun disconnect() {
        tvConnectionRepository.disconnect(viewModelScope)
    }

    fun checkConnectionAndReconnect() {
        tvConnectionRepository.checkConnectionAndReconnect(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        tvDiscoveryRepository.stopDiscovery()
    }
}
