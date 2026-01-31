package com.auto_wifi_postman.data.wifi

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.auto_wifi_postman.data.repository.KnownNetworksRepository
import com.auto_wifi_postman.util.LogTags
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import android.net.wifi.ScanResult
import android.content.pm.PackageManager
import android.os.Build


@Singleton
class WifiScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager,
    private val repository: KnownNetworksRepository
) {
    private fun hasWifiPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    suspend fun scanKnownNetworks(): List<ScanResult> {

        Log.i(LogTags.WIFI_SCAN, "=== Wi-Fi scan started ===")

        if (!hasWifiPermission()) {
            Log.w(LogTags.WIFI_SCAN, "Wi-Fi permission missing → empty scan")
            return emptyList()
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!lm.isLocationEnabled) {
            Log.w(LogTags.WIFI_SCAN, "Location disabled → empty scan")
            return emptyList()
        }

        val knownSsids = repository.getAll()
            .map { it.ssid }
            .toSet()

        wifiManager.startScan()
        delay(5_000) // 5 секунд надёжнее, чем 3

        val results = wifiManager.scanResults
            .filter { it.SSID.isNotBlank() }
            .filter { it.SSID in knownSsids }

        Log.i(
            LogTags.WIFI_SCAN,
            "Known scan results: ${results.map { "${it.SSID} (${it.level})" }}"
        )

        return results
    }

}

