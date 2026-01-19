package com.auto_wifi_postman.data.http

import android.util.Log
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
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(baseUrl + endpoint)
            val conn = url.openConnection() as HttpURLConnection

            conn.connectTimeout = timeoutMs.toInt()
            conn.readTimeout = timeoutMs.toInt()
            conn.requestMethod = "GET"

            val code = conn.responseCode
            Log.i(LogTags.HTTP, "HTTP code = $code")
            code in 200..299
        } catch (e: Exception) {
            Log.e(LogTags.HTTP, "HTTP error", e)
            false
        }
    }
}

