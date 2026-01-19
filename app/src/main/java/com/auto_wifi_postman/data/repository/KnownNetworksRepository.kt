package com.auto_wifi_postman.data.repository

import com.auto_wifi_postman.domain.model.KnownNetwork

interface KnownNetworksRepository {

    fun getAll(): List<KnownNetwork>

    fun findBySsid(ssid: String): KnownNetwork?
}
