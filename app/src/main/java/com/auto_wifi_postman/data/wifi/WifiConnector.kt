package com.auto_wifi_postman.data.wifi

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.auto_wifi_postman.data.http.LocalHttpClient
import com.auto_wifi_postman.domain.model.KnownNetwork
import com.auto_wifi_postman.util.LogTags
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiConnector @Inject constructor(
    private val httpClient: LocalHttpClient
) {

    suspend fun connectAndCheck(networkConfig: KnownNetwork): Boolean {
        Log.i(
            LogTags.WIFI_CONNECT,
            "Using active Wi-Fi for ${networkConfig.ssid} [${networkConfig.id}]"
        )

        return try {
            httpClient.check(
                baseUrl = networkConfig.baseUrl,
                endpoint = networkConfig.updateEndpoint,
                timeoutMs = networkConfig.timeoutMs
            )
        } catch (t: Throwable) {
            Log.e(
                LogTags.HTTP,
                "HTTP check failed for ${networkConfig.id}",
                t
            )
            false
        }
    }
}




