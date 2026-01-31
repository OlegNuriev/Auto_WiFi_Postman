package com.auto_wifi_postman.data.repository

import com.auto_wifi_postman.domain.model.KnownNetwork
import kotlinx.coroutines.flow.Flow

interface KnownNetworksRepository {

    val networks: Flow<List<KnownNetwork>>

    suspend fun getAll(): List<KnownNetwork>

    suspend fun findBySsid(ssid: String): KnownNetwork?

    suspend fun replaceAll(networks: List<KnownNetwork>)

    suspend fun clear()
}

