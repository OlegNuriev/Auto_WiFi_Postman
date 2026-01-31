package com.auto_wifi_postman.domain.orchestrator

import android.net.wifi.WifiManager
import android.util.Log
import com.auto_wifi_postman.components.ConnectionResult
import com.auto_wifi_postman.data.repository.AgentStatusRepository
import com.auto_wifi_postman.data.repository.KnownNetworksRepository
import com.auto_wifi_postman.data.wifi.WifiConnector
import com.auto_wifi_postman.data.wifi.WifiScanner
import com.auto_wifi_postman.domain.model.KnownNetwork
import com.auto_wifi_postman.domain.state.AgentState
import com.auto_wifi_postman.util.LogTags
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Orchestrator @Inject constructor(
    private val scanner: WifiScanner,
    private val connector: WifiConnector,
    private val repository: KnownNetworksRepository,
    private val statusRepo: AgentStatusRepository,
    private val wifiManager: WifiManager
) {

    private var state: AgentState = AgentState.IDLE

    fun isWifiEnabled(): Boolean =
        wifiManager.isWifiEnabled
    suspend fun startCycle() {
        Log.i(LogTags.ORCHESTRATOR, "=== START CYCLE ===")
        statusRepo.onCycleStarted()
        transition(AgentState.SCANNING)

        val scanResults = try {
            scanner.scanKnownNetworks()
        } catch (t: Throwable) {
            failCycle("Wi-Fi scan failed", t)
            return
        }

        if (scanResults.isEmpty()) {
            failCycle("No known Wi-Fi networks found")
            return
        }

        val bestScan = scanResults.maxByOrNull { it.level }
            ?: run {
                failCycle("Unable to select strongest Wi-Fi")
                return
            }

        Log.i(
            LogTags.ORCHESTRATOR,
            "Best network: ${bestScan.SSID}, level=${bestScan.level}"
        )

        val network = repository.getAll()
            .firstOrNull { it.ssid == bestScan.SSID }

        if (network == null) {
            failCycle("No config for SSID ${bestScan.SSID}")
            return
        }

        val result = processNetwork(network)

        val nextCycleAt = System.currentTimeMillis() + ONE_HOUR_MS

        statusRepo.onCycleFinished(
            success = result is ConnectionResult.Success,
            nextCycleAt = nextCycleAt
        )

        transition(AgentState.SLEEP)
    }

    suspend fun scanOnce(): List<KnownNetwork> =
        scanner.scanKnownNetworks()
            .sortedByDescending { it.level }
            .mapNotNull { scan ->
                repository.getAll().firstOrNull { it.ssid == scan.SSID }
            }

    suspend fun processSingle(networkId: String): ConnectionResult {
        val network = repository.getAll()
            .firstOrNull { it.id == networkId }
            ?: return ConnectionResult.Failure("Network not found")

        return processNetwork(network)
    }

    private suspend fun processNetwork(network: KnownNetwork): ConnectionResult {
        transition(AgentState.CONNECTING)

        val result = connector.connectAndCheck(network)

        when (result) {
            is ConnectionResult.Success ->
                Log.i(LogTags.ORCHESTRATOR, "SUCCESS on ${network.id}")

            is ConnectionResult.Failure ->
                Log.e(
                    LogTags.ORCHESTRATOR,
                    "FAILED ${network.id}: ${result.reason}"
                )
        }

        return result
    }

    private fun failCycle(reason: String, t: Throwable? = null) {
        Log.e(LogTags.ORCHESTRATOR, reason, t)

        statusRepo.onCycleFinished(
            success = false,
            nextCycleAt = System.currentTimeMillis() + ONE_HOUR_MS
        )

        transition(AgentState.SLEEP)
    }

    private fun transition(newState: AgentState) {
        state = newState
        Log.i(LogTags.ORCHESTRATOR, "STATE → $state")
    }

    companion object {
        private const val ONE_HOUR_MS = 60 * 60 * 1000L
    }
}
