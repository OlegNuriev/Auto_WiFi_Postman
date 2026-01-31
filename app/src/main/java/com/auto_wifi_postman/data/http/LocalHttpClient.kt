package com.auto_wifi_postman.data.http

import android.util.Log
import com.auto_wifi_postman.components.ConnectionResult
import com.auto_wifi_postman.util.LogTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class LocalHttpClient @Inject constructor() {

    suspend fun check(
        baseUrl: String,
        endpoint: String,
        timeoutMs: Long
    ): ConnectionResult = withContext(Dispatchers.IO) {

        return@withContext try {
            val url = URL(baseUrl + endpoint)
            val conn = url.openConnection() as HttpURLConnection

            conn.connectTimeout = timeoutMs.toInt()
            conn.readTimeout = timeoutMs.toInt()
            conn.requestMethod = "GET"

            val code = conn.responseCode
            Log.i(LogTags.HTTP, "HTTP code = $code")

            if (code in 200..299) {
                ConnectionResult.Success
            } else {
                ConnectionResult.Failure("HTTP $code")
            }
        } catch (e: Exception) {
            Log.w(LogTags.HTTP, "HTTP failed", e)
            ConnectionResult.Failure(e.message ?: "Network error")
        }
    }
}