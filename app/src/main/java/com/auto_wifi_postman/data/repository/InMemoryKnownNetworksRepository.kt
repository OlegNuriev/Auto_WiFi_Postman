package com.auto_wifi_postman.data.repository

import com.auto_wifi_postman.domain.model.KnownNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryKnownNetworksRepository @Inject constructor(
    private val persistentRepo: KnownNetworksRepository
) : KnownNetworksRepository {

    private val _cache = MutableStateFlow<List<KnownNetwork>>(emptyList())

    override val networks: Flow<List<KnownNetwork>> = _cache.asStateFlow()

    override suspend fun getAll(): List<KnownNetwork> = _cache.value

    override suspend fun findBySsid(ssid: String): KnownNetwork? =
        _cache.value.firstOrNull { it.ssid == ssid }

    override suspend fun replaceAll(networks: List<KnownNetwork>) {
        persistentRepo.replaceAll(networks)
        _cache.value = networks
    }

    override suspend fun clear() {
        persistentRepo.clear()
        _cache.value = emptyList()
    }

    suspend fun loadFromStorage() {
        _cache.value = persistentRepo.getAll()
    }
}

