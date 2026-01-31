package com.auto_wifi_postman.data.wifi

import android.util.Log
import com.auto_wifi_postman.components.ConnectionResult
import com.auto_wifi_postman.data.http.LocalHttpClient
import com.auto_wifi_postman.domain.model.KnownNetwork
import com.auto_wifi_postman.util.LogTags
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiConnector @Inject constructor(
    private val httpClient: LocalHttpClient
) {

    suspend fun connectAndCheck(network: KnownNetwork): ConnectionResult {
        Log.i(
            LogTags.WIFI_CONNECT,
            "Using active Wi-Fi for ${network.ssid} [${network.id}]"
        )

        val result = httpClient.check(
            baseUrl = network.baseUrl,
            endpoint = network.updateEndpoint,
            timeoutMs = network.timeoutMs
        )

        return when (result) {
            is ConnectionResult.Success -> {
                Log.i(LogTags.WIFI_CONNECT, "✅ SUCCESS for ${network.id}")
                result
            }

            is ConnectionResult.Failure -> {
                Log.w(
                    LogTags.WIFI_CONNECT,
                    "❌ FAILED for ${network.id}: ${result.reason}"
                )
                result
            }
        }
    }
}
