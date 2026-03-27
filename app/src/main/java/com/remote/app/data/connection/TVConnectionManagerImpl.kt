package com.remote.app.data.connection

import android.content.Context
import android.util.Log
import com.kunal52.AndroidRemoteContext
import com.kunal52.AndroidRemoteTv
import com.kunal52.AndroidTvListener
import com.kunal52.remote.Remotemessage
import com.remote.app.BuildConfig
import com.remote.app.domain.AppConstants
import com.remote.app.domain.model.ConnectionState
import com.remote.app.domain.model.ConnectionStep
import com.remote.app.domain.model.DiscoveredTV
import com.remote.app.domain.repository.TVConnectionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TVConnectionManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TVConnectionRepository {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _connectionStep = MutableStateFlow<ConnectionStep>(ConnectionStep.Idle)
    override val connectionStep: StateFlow<ConnectionStep> = _connectionStep

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage

    private val _latencyMs = MutableStateFlow<Long?>(null)
    override val latencyMs: StateFlow<Long?> = _latencyMs

    private var androidRemoteTv: AndroidRemoteTv? = null
    private var connectedTv: DiscoveredTV? = null
    private var connectJob: Job? = null

    init {
        AndroidRemoteContext.getInstance().initialize(context)
        AndroidRemoteContext.getInstance().clientName = AppConstants.CLIENT_NAME
    }

    override fun connectToTv(tv: DiscoveredTV, scope: CoroutineScope) {
        // Clean up any previous session first
        val oldRemote = androidRemoteTv
        connectJob?.cancel()
        androidRemoteTv = null

        connectedTv = tv
        _connectionState.value = ConnectionState.CONNECTING

        // Disconnect old session in background
        if (oldRemote != null) {
            scope.launch(Dispatchers.IO) {
                try { oldRemote.disconnect() } catch (_: Exception) {}
            }
        }

        val newRemote = AndroidRemoteTv()
        androidRemoteTv = newRemote

        connectJob = scope.launch(Dispatchers.IO) {
            try {
                newRemote.connect(tv.host, object : AndroidTvListener {
                    private fun isActive() = androidRemoteTv === newRemote

                    override fun onSessionCreated() {}
                    
                    override fun onSecretRequested() {
                        if (!isActive()) return
                        _connectionState.value = ConnectionState.PAIRING_PIN_REQUESTED
                    }
                    
                    override fun onPaired() {
                        if (!isActive()) return
                        if (BuildConfig.DEBUG) Log.d(TAG, "Paired successfully")
                    }
                    
                    override fun onConnectingToRemote() {
                        if (!isActive()) return
                        _connectionState.value = ConnectionState.CONNECTING
                    }
                    
                    override fun onConnected() {
                        if (!isActive()) return
                        _connectionStep.value = ConnectionStep.Idle
                        _connectionState.value = ConnectionState.CONNECTED
                    }
                    
                    override fun onDisconnect() {
                        if (!isActive()) return
                        if (_connectionState.value == ConnectionState.CONNECTED) {
                            _connectionStep.value = ConnectionStep.Idle
                            _connectionState.value = ConnectionState.DISCONNECTED
                        }
                    }
                    
                    override fun onError(error: String?) {
                        if (!isActive()) return
                        if (BuildConfig.DEBUG) Log.e(TAG, "Remote error: $error")
                        
                        // If the TV rejected our certificate, pairing was lost or invalid on their end.
                        // Wipe our local keystore to force a new pairing session on next connect.
                        if (error?.contains("SSL", ignoreCase = true) == true || 
                            error?.contains("CERTIFICATE", ignoreCase = true) == true) {
                            try {
                                AndroidRemoteContext.getInstance().keyStoreFile?.delete()
                                _errorMessage.value = "Pairing lost or TV rejected connection. Please connect again to re-pair."
                            } catch (e: Exception) {
                                if (BuildConfig.DEBUG) Log.e(TAG, "Failed to delete keystore", e)
                                _errorMessage.value = error
                            }
                        } else {
                            _errorMessage.value = error
                        }

                        connectedTv = null
                        _connectionStep.value = ConnectionStep.Idle
                        _connectionState.value = ConnectionState.ERROR
                    }
                    
                    override fun onConnectionStep(step: String) {
                        if (!isActive()) return
                        _connectionStep.value = when (step) {
                            "initiating"  -> ConnectionStep.InitiatingConnection
                            "connecting"  -> ConnectionStep.EstablishingEncryption
                            "handshake"   -> ConnectionStep.Authenticating
                            "negotiating" -> ConnectionStep.NegotiatingSession
                            "activating"  -> ConnectionStep.ActivatingRemote
                            else          -> ConnectionStep.Idle
                        }
                    }
                })
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Connect error", e)
                _errorMessage.value = e.message
                connectedTv = null
                _connectionStep.value = ConnectionStep.Idle
                _connectionState.value = ConnectionState.ERROR
            }
        }
    }

    override fun sendSecret(pin: String, scope: CoroutineScope) {
        _connectionState.value = ConnectionState.CONNECTING
        scope.launch(Dispatchers.IO) {
            try {
                androidRemoteTv?.sendSecret(pin)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "sendSecret error", e)
                _errorMessage.value = e.message ?: "Invalid PIN"
                _connectionState.value = ConnectionState.ERROR
            }
        }
    }

    override fun sendCommand(keyCode: Int, scope: CoroutineScope) {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            scope.launch(Dispatchers.IO) {
                try {
                    val remoteKeyCode = Remotemessage.RemoteKeyCode.forNumber(keyCode)
                    val start = System.nanoTime()
                    androidRemoteTv?.sendCommand(remoteKeyCode, Remotemessage.RemoteDirection.SHORT)
                    val elapsed = (System.nanoTime() - start) / 1_000_000
                    _latencyMs.value = elapsed
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "sendCommand error", e)
                    disconnect(scope)
                }
            }
        }
    }

    override fun disconnect(scope: CoroutineScope) {
        val remote = androidRemoteTv
        connectJob?.cancel()
        connectJob = null
        androidRemoteTv = null
        connectedTv = null
        _connectionStep.value = ConnectionStep.Idle
        _connectionState.value = ConnectionState.DISCONNECTED
        scope.launch(Dispatchers.IO) {
            try {
                remote?.disconnect()
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "disconnect error", e)
            }
        }
    }

    override fun resetState() {
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override fun checkConnectionAndReconnect(scope: CoroutineScope) {
        // Only reconnect automatically when cleanly DISCONNECTED, not on ERROR.
        // ERROR means the last attempt failed — the user must retry explicitly to avoid
        // an infinite silent reconnect loop (ECONNABORTED / timeout → ERROR → ON_RESUME → retry → ...).
        if (_connectionState.value == ConnectionState.DISCONNECTED) {
            connectedTv?.let { tv ->
                connectToTv(tv, scope)
            }
        }
    }

    companion object {
        private const val TAG = "TVConnectionManager"
    }
}
