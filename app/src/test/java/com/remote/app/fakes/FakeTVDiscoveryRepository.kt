package com.remote.app.fakes

import com.remote.app.domain.model.DiscoveredTV
import com.remote.app.domain.repository.TVDiscoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeTVDiscoveryRepository : TVDiscoveryRepository {

    val _discoveredTVs = MutableStateFlow<List<DiscoveredTV>>(emptyList())
    override val discoveredTVs: StateFlow<List<DiscoveredTV>> = _discoveredTVs

    var startDiscoveryCalled = false
    var stopDiscoveryCalled = false

    override fun startDiscovery() {
        startDiscoveryCalled = true
    }

    override fun stopDiscovery() {
        stopDiscoveryCalled = true
    }
}
