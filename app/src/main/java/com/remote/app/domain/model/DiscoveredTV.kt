package com.remote.app.domain.model

data class DiscoveredTV(
    val name: String,
    val host: String,
    val port: Int,
    val lastSeenMillis: Long = 0L
)
