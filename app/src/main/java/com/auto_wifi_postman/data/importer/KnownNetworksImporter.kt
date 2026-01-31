package com.auto_wifi_postman.data.importer

import android.content.Context
import android.net.Uri
import com.auto_wifi_postman.domain.model.KnownNetwork
import com.auto_wifi_postman.domain.validation.KnownNetworkValidator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class KnownNetworksImporter(
    private val context: Context
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun import(uri: Uri): Result<List<KnownNetwork>> =
        runCatching {

            val text = context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: error("File is empty")

            val parsed = json.decodeFromString<List<KnownNetwork>>(text)

            val invalid = parsed
                .map { it to KnownNetworkValidator.validate(it) }
                .filter { it.second.isNotEmpty() }

            if (invalid.isNotEmpty()) {
                val message = invalid.joinToString("\n") {
                    "${it.first.id}: ${it.second.joinToString()}"
                }
                error("Validation failed:\n$message")
            }

            parsed
        }
}
