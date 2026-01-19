package com.auto_wifi_postman.domain.orchestrator

import android.util.Log
import com.auto_wifi_postman.data.repository.KnownNetworksRepository
import com.auto_wifi_postman.data.wifi.WifiConnector
import com.auto_wifi_postman.data.wifi.WifiScanner
import com.auto_wifi_postman.domain.state.AgentState
import com.auto_wifi_postman.util.LogTags
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Orchestrator @Inject constructor(
    private val scanner: WifiScanner,
    private val connector: WifiConnector,
    private val repository: KnownNetworksRepository
) {

    private var state: AgentState = AgentState.IDLE

    suspend fun startCycle() {
        Log.i(LogTags.ORCHESTRATOR, "=== START CYCLE ===")
        transition(AgentState.SCANNING)

        val foundSsids = try {
            scanner.scanKnownSsids()
        } catch (t: Throwable) {
            Log.e(LogTags.ORCHESTRATOR, "Wi-Fi scan failed", t)
            emptyList()
        }

        Log.i(LogTags.ORCHESTRATOR, "Scan result SSIDs: $foundSsids")

        if (foundSsids.isEmpty()) {
            Log.i(LogTags.ORCHESTRATOR, "No known networks found")
            goToSleep()
            return
        }

        transition(AgentState.FOUND_NETWORKS)

        val targets = repository.getAll()
            .filter { it.ssid in foundSsids }

        Log.i(
            LogTags.ORCHESTRATOR,
            "Targets to process: ${targets.map { it.id }}"
        )

        for (config in targets) {
            transition(AgentState.CONNECTING)

            Log.i(
                LogTags.ORCHESTRATOR,
                "→ PROCESS ${config.id} (SSID=${config.ssid})"
            )

            var success = false

            repeat(config.retries) { attempt ->
                Log.i(
                    LogTags.ORCHESTRATOR,
                    "Attempt ${attempt + 1}/${config.retries} for ${config.id}"
                )

                success = try {
                    connector.connectAndCheck(config)
                } catch (t: Throwable) {
                    Log.e(
                        LogTags.ORCHESTRATOR,
                        "Error during connect/check for ${config.id}",
                        t
                    )
                    false
                }

                if (success) {
                    Log.i(
                        LogTags.ORCHESTRATOR,
                        "SUCCESS on ${config.id}"
                    )
                    return@repeat
                }
            }

            if (!success) {
                Log.w(
                    LogTags.ORCHESTRATOR,
                    "FAILED ${config.id} after ${config.retries} attempts"
                )
            }

            transition(AgentState.DISCONNECT)

            delay(3_000)
        }

        goToSleep()
    }

    private fun goToSleep() {
        transition(AgentState.SLEEP)
        Log.i(LogTags.ORCHESTRATOR, "Cycle finished, going to sleep")
    }

    private fun transition(newState: AgentState) {
        state = newState
        Log.i(LogTags.ORCHESTRATOR, "STATE → $state")
    }
}
