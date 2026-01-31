package com.auto_wifi_postman.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.auto_wifi_postman.data.datastore.networksDataStore
import com.auto_wifi_postman.domain.model.KnownNetwork
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnownNetworksRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : KnownNetworksRepository {

    private val KEY_NETWORKS = stringPreferencesKey("networks_json")

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    override val networks: Flow<List<KnownNetwork>> =
        context.networksDataStore.data.map { prefs ->
            val stored = prefs[KEY_NETWORKS]
            if (stored.isNullOrBlank()) {
                emptyList()
            } else {
                runCatching {
                    json.decodeFromString<List<KnownNetwork>>(stored)
                }.getOrElse {
                    emptyList()
                }
            }
        }

    override suspend fun getAll(): List<KnownNetwork> =
        networks.first()

    override suspend fun findBySsid(ssid: String): KnownNetwork? =
        getAll().firstOrNull { it.ssid == ssid }

    override suspend fun replaceAll(networks: List<KnownNetwork>) {
        context.networksDataStore.edit { prefs ->
            prefs[KEY_NETWORKS] = json.encodeToString(networks)
        }
    }

    override suspend fun clear() {
        context.networksDataStore.edit { prefs ->
            prefs.remove(KEY_NETWORKS)
        }
    }
}
