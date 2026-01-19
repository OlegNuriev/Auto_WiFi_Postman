package com.auto_wifi_postman.domain.model

data class KnownNetwork(
    val id: String,
    val ssid: String,
    val password: String?,
    val baseUrl: String,
    val updateEndpoint: String,
    val timeoutMs: Long,
    val retries: Int
)


