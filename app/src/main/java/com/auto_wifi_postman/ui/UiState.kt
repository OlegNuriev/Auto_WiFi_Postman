package com.auto_wifi_postman.ui

import com.auto_wifi_postman.components.ConnectionResult
import com.auto_wifi_postman.domain.model.KnownNetwork
import com.auto_wifi_postman.domain.state.AgentStatus
import com.auto_wifi_postman.domain.state.AppMode
import com.auto_wifi_postman.domain.state.AutoStatus

data class UiState(

    val mode: AppMode = AppMode.AUTO,

    val networks: List<KnownNetwork> = emptyList(),

    val isBusy: Boolean = false,

    val manualResult: ConnectionResult? = null,

    val hasNetworks: Boolean = false,

    val importSuccess: Boolean = false,

    val importError: String? = null,

    val agentStatus: AgentStatus? = null,

    val autoStatus: AutoStatus? = null,

    val lastErrorMessage: String? = null,

    val bannerMessage: String? = null,
    val bannerLevel: BannerLevel? = null,

    val manualScanResults: List<KnownNetwork> = emptyList(),

    val autoCountdownMs: Long = 0L,

    val requestImportJson: Boolean = false,

    val isImportMenuOpen: Boolean = false


)
