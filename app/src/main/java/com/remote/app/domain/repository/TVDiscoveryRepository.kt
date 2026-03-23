package com.remote.app.domain.repository

import com.remote.app.domain.model.DiscoveredTV
import kotlinx.coroutines.flow.StateFlow

interface TVDiscoveryRepository {
    val discoveredTVs: StateFlow<List<DiscoveredTV>>
    fun startDiscovery()
    fun stopDiscovery()
}
