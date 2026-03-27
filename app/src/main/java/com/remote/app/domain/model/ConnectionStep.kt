package com.remote.app.domain.model

/**
 * Granular progress steps shown to the user while connecting to a TV.
 * Each step maps to a human-readable message displayed below the spinner.
 */
sealed class ConnectionStep {
    data object Idle : ConnectionStep()
    data object InitiatingConnection : ConnectionStep()
    data object EstablishingEncryption : ConnectionStep()
    data object Authenticating : ConnectionStep()
    data object NegotiatingSession : ConnectionStep()
    data object ActivatingRemote : ConnectionStep()
}
