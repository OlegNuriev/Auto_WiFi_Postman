package com.auto_wifi_postman.data.repository

import android.content.Context
import com.auto_wifi_postman.domain.model.KnownNetwork
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FileKnownNetworksSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val file = File(context.filesDir, "known_networks.json")

    fun exists(): Boolean = file.exists()

    fun load(): List<KnownNetwork> {
        val json = file.readText()
        return Json.decodeFromString(json)
    }

    fun save(json: String) {
        file.writeText(json)
    }
}
