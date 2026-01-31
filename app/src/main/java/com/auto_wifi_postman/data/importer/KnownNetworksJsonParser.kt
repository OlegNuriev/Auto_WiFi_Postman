package com.auto_wifi_postman.data.importer

import com.auto_wifi_postman.domain.model.KnownNetwork
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnownNetworksJsonParser @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parse(raw: String): ImportResult {
        if (raw.isBlank()) {
            return ImportResult.Error("Файл пустой")
        }

        val networks = try {
            json.decodeFromString<List<KnownNetwork>>(raw)
        } catch (e: SerializationException) {
            return ImportResult.Error(
                "Ошибка JSON: ${e.message ?: "Ошибка разбора"}"
            )
        } catch (e: Exception) {
            return ImportResult.Error(
                "Неожиданная ошибка: ${e.message ?: e::class.simpleName}"
            )
        }

        if (networks.isEmpty()) {
            return ImportResult.Error("Список сетей пуст")
        }

        networks.forEachIndexed { index, net ->

            if (net.id.isBlank())
                return ImportResult.Error("[$index] id не может быть пустым")

            if (net.ssid.isBlank())
                return ImportResult.Error("[$index] ssid не может быть пустым")

            if (net.baseUrl.isBlank())
                return ImportResult.Error("[$index] baseUrl не может быть пустым")

            if (
                !net.baseUrl.startsWith("http://", true) &&
                !net.baseUrl.startsWith("https://", true)
            ) {
                return ImportResult.Error(
                    "[$index] baseUrl должен начинаться с http:// или https://"
                )
            }

            if (net.updateEndpoint.isBlank())
                return ImportResult.Error("[$index] updateEndpoint не может быть пустым")

            if (net.timeoutMs <= 0)
                return ImportResult.Error("[$index] timeoutMs должен быть > 0")

            if (net.retries <= 0)
                return ImportResult.Error("[$index] retries должен быть > 0")

        }

        return ImportResult.Success(networks)
    }
}
