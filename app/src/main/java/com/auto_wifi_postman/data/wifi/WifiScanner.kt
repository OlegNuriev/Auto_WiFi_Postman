package com.auto_wifi_postman.data.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.auto_wifi_postman.data.repository.KnownNetworksRepository
import com.auto_wifi_postman.util.LogTags
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class WifiScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager,
    private val repository: KnownNetworksRepository
) {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    suspend fun scanKnownSsids(): List<String> =
        suspendCancellableCoroutine { cont ->

            Log.i(LogTags.WIFI_SCAN, "Starting Wi-Fi scan")

            val lm =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            Log.i(
                LogTags.WIFI_SCAN,
                "Location enabled = ${lm.isLocationEnabled}"
            )

            val knownSsids = repository.getAll()
                .map { it.ssid }
                .toSet()

            Log.i(LogTags.WIFI_SCAN, "Known SSIDs: $knownSsids")

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    if (intent.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) return

                    val rawResults = wifiManager.scanResults
                        .map { it.SSID }
                        .filter { it.isNotBlank() }

                    Log.i(
                        LogTags.WIFI_SCAN,
                        "Raw scan results: $rawResults"
                    )

                    val filtered = rawResults.filter { it in knownSsids }

                    Log.i(
                        LogTags.WIFI_SCAN,
                        "Filtered known SSIDs: $filtered"
                    )

                    safelyResume(filtered)
                }

                private fun safelyResume(result: List<String>) {
                    try {
                        context.unregisterReceiver(this)
                    } catch (_: Exception) {
                        Log.w(
                            LogTags.WIFI_SCAN,
                            "Receiver unregister failed (already unregistered)"
                        )
                    }

                    if (cont.isActive) {
                        cont.resume(result)
                    }
                }
            }

            context.registerReceiver(
                receiver,
                IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            )

            val started = wifiManager.startScan()
            Log.i(LogTags.WIFI_SCAN, "startScan() returned $started")

            if (!started) {
                Log.w(
                    LogTags.WIFI_SCAN,
                    "Wi-Fi scan was not started, using cached results"
                )

                val cached = wifiManager.scanResults
                    .map { it.SSID }
                    .filter { it.isNotBlank() && it in knownSsids }

                Log.i(
                    LogTags.WIFI_SCAN,
                    "Cached scan results: $cached"
                )

                try {
                    context.unregisterReceiver(receiver)
                } catch (_: Exception) {}

                cont.resume(cached)
                return@suspendCancellableCoroutine
            }

            cont.invokeOnCancellation {
                Log.w(LogTags.WIFI_SCAN, "Scan coroutine cancelled")
                try {
                    context.unregisterReceiver(receiver)
                } catch (_: Exception) {}
            }
        }
}
