package com.auto_wifi_postman.data.repository

import android.content.Context
import com.auto_wifi_postman.domain.model.KnownNetwork
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonKnownNetworksRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : KnownNetworksRepository {

    private val cache: List<KnownNetwork> by lazy { load() }

    override fun getAll(): List<KnownNetwork> = cache

    override fun findBySsid(ssid: String): KnownNetwork? =
        cache.firstOrNull { it.ssid == ssid }

    private fun load(): List<KnownNetwork> {
        val json = context.assets
            .open("known_networks.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(json)
        val array = root.getJSONArray("networks")

        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)

            KnownNetwork(
                id = obj.getString("id"),
                ssid = obj.getString("ssid"),
                password = obj.optString("password", null),
                baseUrl = obj.getString("baseUrl"),
                updateEndpoint = obj.getString("updateEndpoint"),
                timeoutMs = obj.getLong("timeoutMs"),
                retries = obj.getInt("retries")
            )
        }
    }
}
