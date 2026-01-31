package com.auto_wifi_postman.components

import com.auto_wifi_postman.domain.model.KnownNetwork

sealed interface AppEvent {

    data object SwitchToAuto : AppEvent
    data object SwitchToManual : AppEvent
    data object StartManualScan : AppEvent

    data class SelectNetwork(val network: KnownNetwork) : AppEvent
    data class ImportNetworks(val json: String) : AppEvent
    data class ConnectNetwork(val networkId: String) : AppEvent

    object ToggleImportMenu : AppEvent

    object ImportJsonClicked : AppEvent
}
