package com.remote.app.connection

import android.content.Context
import android.util.Log
import com.kunal52.AndroidRemoteContext
import com.kunal52.AndroidRemoteTv
import com.kunal52.AndroidTvListener
import com.kunal52.remote.Remotemessage
import com.remote.app.network.DiscoveredTV
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TVConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var androidRemoteTv: AndroidRemoteTv? = null
    private var connectedTv: DiscoveredTV? = null

    init {
        AndroidRemoteContext.getInstance().initialize(context)
        AndroidRemoteContext.getInstance().clientName = "Minimal TV Remote"
    }

    fun connectToTv(tv: DiscoveredTV, scope: CoroutineScope) {
        connectedTv = tv
        _connectionState.value = ConnectionState.CONNECTING

        androidRemoteTv = AndroidRemoteTv()
        scope.launch(Dispatchers.IO) {
            try {
                androidRemoteTv?.connect(tv.host, object : AndroidTvListener {
                    override fun onSessionCreated() {}

                    override fun onSecretRequested() {
                        _connectionState.value = ConnectionState.PAIRING_PIN_REQUESTED
                    }

                    override fun onPaired() {
                        Log.d("TVConnectionManager", "Paired successfully")
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
                Log.e("TVConnectionManager", "Connect error", e)
                _errorMessage.value = e.message
                _connectionState.value = ConnectionState.ERROR
            }
        }
    }

    fun sendSecret(pin: String, scope: CoroutineScope) {
        _connectionState.value = ConnectionState.CONNECTING
        scope.launch(Dispatchers.IO) {
            androidRemoteTv?.sendSecret(pin)
        }
    }

    fun sendCommand(keyCode: Remotemessage.RemoteKeyCode, scope: CoroutineScope) {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            scope.launch(Dispatchers.IO) {
                try {
                    androidRemoteTv?.sendCommand(keyCode, Remotemessage.RemoteDirection.SHORT)
                } catch (e: Exception) {
                    Log.e("TVConnectionManager", "sendCommand error (Broken pipe / TV slept)", e)
                    disconnect(scope)
                }
            }
        }
    }

    fun disconnect(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            androidRemoteTv?.disconnect()
        }
        connectedTv = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun resetState() {
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun checkConnectionAndReconnect(scope: CoroutineScope) {
        if (_connectionState.value == ConnectionState.ERROR || _connectionState.value == ConnectionState.DISCONNECTED) {
            connectedTv?.let { tv ->
                connectToTv(tv, scope)
            }
        }
    }
}
