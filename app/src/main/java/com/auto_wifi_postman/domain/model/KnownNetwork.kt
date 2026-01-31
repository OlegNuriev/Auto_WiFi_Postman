package com.auto_wifi_postman.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class KnownNetwork(
    val id: String,
    val ssid: String,
    val password: String? = null,
    val baseUrl: String,
    val updateEndpoint: String,
    val retries: Int = 2,
    val timeoutMs: Long = 5_000
)


