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
import com.remote.app.domain.model.DiscoveredTV
import com.remote.app.domain.repository.TVConnectionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage

    private var androidRemoteTv: AndroidRemoteTv? = null
    private var connectedTv: DiscoveredTV? = null

    init {
        AndroidRemoteContext.getInstance().initialize(context)
        AndroidRemoteContext.getInstance().clientName = AppConstants.CLIENT_NAME
    }

    override fun connectToTv(tv: DiscoveredTV, scope: CoroutineScope) {
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
                        if (BuildConfig.DEBUG) Log.d(TAG, "Paired successfully")
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
                if (BuildConfig.DEBUG) Log.e(TAG, "Connect error", e)
                _errorMessage.value = e.message
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
                    androidRemoteTv?.sendCommand(remoteKeyCode, Remotemessage.RemoteDirection.SHORT)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "sendCommand error", e)
                    disconnect(scope)
                }
            }
        }
    }

    override fun disconnect(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            androidRemoteTv?.disconnect()
        }
        connectedTv = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override fun resetState() {
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override fun checkConnectionAndReconnect(scope: CoroutineScope) {
        if (_connectionState.value == ConnectionState.ERROR || _connectionState.value == ConnectionState.DISCONNECTED) {
            connectedTv?.let { tv ->
                connectToTv(tv, scope)
            }
        }
    }

    companion object {
        private const val TAG = "TVConnectionManager"
    }
}
