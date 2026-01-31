package com.auto_wifi_postman.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auto_wifi_postman.components.AppEvent
import com.auto_wifi_postman.components.ConnectionResult
import com.auto_wifi_postman.data.importer.ImportResult
import com.auto_wifi_postman.data.importer.KnownNetworksJsonParser
import com.auto_wifi_postman.data.repository.AgentStatusRepository
import com.auto_wifi_postman.data.repository.AppModeRepository
import com.auto_wifi_postman.data.repository.ImportStateRepository
import com.auto_wifi_postman.data.repository.InMemoryKnownNetworksRepository
import com.auto_wifi_postman.domain.orchestrator.Orchestrator
import com.auto_wifi_postman.domain.state.AppMode
import com.auto_wifi_postman.service.AgentForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modeRepo: AppModeRepository,
    private val importStateRepo: ImportStateRepository,
    private val networksRepo: InMemoryKnownNetworksRepository,
    private val parser: KnownNetworksJsonParser,
    private val statusRepo: AgentStatusRepository,
    private val orchestrator: Orchestrator,


) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {

        viewModelScope.launch {
            combine(
                importStateRepo.hasImportedOnce,
                modeRepo.mode
            ) { imported, mode ->
                imported to mode
            }.collect { (imported, mode) ->
                _state.update {
                    it.copy(
                        hasNetworks = imported,
                        mode = if (!imported) AppMode.INIT else mode
                    )
                }
            }
        }

        viewModelScope.launch {
            networksRepo.loadFromStorage()
            _state.update { it.copy(networks = networksRepo.getAll()) }
        }

        viewModelScope.launch {
            statusRepo.status.collect { status ->

                if (
                    status != null &&
                    state.value.mode == AppMode.AUTO &&
                    !status.isRunning &&
                    status.nextCycleAt > System.currentTimeMillis()
                ) {
                    startAutoCountdown(status.nextCycleAt)
                }

                if (state.value.mode != AppMode.AUTO || status?.isRunning == true) {
                    stopAutoCountdown()
                }

                val banner = if (status != null && status.lastSuccess == false) {
                    "⚠️ Авто-проверка не удалась. Сервер недоступен ⚠️"
                } else null

                _state.update {
                    it.copy(
                        agentStatus = status,
                        bannerMessage = banner,
                        bannerLevel = if (banner != null) BannerLevel.WARNING else null
                    )
                }
            }
        }


        viewModelScope.launch {
            if (
                importStateRepo.hasImportedOnce.first() &&
                modeRepo.mode.first() == AppMode.AUTO
            ) {
                AgentForegroundService.start(context)
            }
        }
    }

    fun onEvent(event: AppEvent) = viewModelScope.launch {
        when (event) {

            AppEvent.ToggleImportMenu -> {
                _state.update {
                    it.copy(isImportMenuOpen = !it.isImportMenuOpen)
                }
            }

            AppEvent.ImportJsonClicked -> {
                _state.update {
                    it.copy(
                        requestImportJson = true,
                        isImportMenuOpen = false,
                        bannerMessage = "📥 Выберите JSON файл",
                        bannerLevel = BannerLevel.INFO
                    )
                }
            }

            is AppEvent.ImportNetworks -> {
                _state.update {
                    it.copy(
                        bannerMessage = "📥 Импортирую сети…",
                        bannerLevel = BannerLevel.INFO
                    )
                }
                handleImport(event.json)
            }

            AppEvent.SwitchToManual -> {
                modeRepo.setManual()
                stopAutoCountdown()
                _state.update {
                    it.copy(
                        bannerMessage = "🧭 Ручной режим. Автопроверка остановлена",
                        bannerLevel = BannerLevel.INFO
                    )
                }
            }

            AppEvent.SwitchToAuto -> {
                modeRepo.setAuto()
                AgentForegroundService.start(context)
                _state.update {
                    it.copy(
                        bannerMessage = "🤖 Автоматический режим активен",
                        bannerLevel = BannerLevel.INFO
                    )
                }
            }

            AppEvent.StartManualScan -> {

                if (!orchestrator.isWifiEnabled()) {
                    _state.update {
                        it.copy(
                            manualScanResults = emptyList(),
                            bannerMessage = "📴 Wi-Fi выключен. Включите Wi-Fi и повторите",
                            bannerLevel = BannerLevel.WARNING,
                            isBusy = false
                        )
                    }
                    return@launch
                }

                _state.update {
                    it.copy(
                        isBusy = true,
                        bannerMessage = "📡 Ищу доступные Wi-Fi сети…",
                        bannerLevel = BannerLevel.INFO
                    )
                }

                val scanResults = orchestrator.scanOnce()

                _state.update {
                    it.copy(
                        manualScanResults = scanResults,
                        bannerMessage = if (scanResults.isEmpty())
                            "⚠️ Поблизости нет сохранённых Wi-Fi сетей"
                        else
                            "📋 Выберите сеть для отправки запроса",
                        bannerLevel = if (scanResults.isEmpty())
                            BannerLevel.WARNING
                        else
                            BannerLevel.INFO,
                        isBusy = false
                    )
                }
            }

            is AppEvent.SelectNetwork -> {
                runManualConnect(event.network.id)
            }

            is AppEvent.ConnectNetwork -> {
                runManualConnect(event.networkId)
            }

            else -> Unit
        }
    }

    private suspend fun runManualConnect(networkId: String) {
        _state.update {
            it.copy(
                isBusy = true,
                bannerMessage = "⏳ Отправляю HTTP запрос…",
                bannerLevel = BannerLevel.INFO
            )
        }

        val result = orchestrator.processSingle(networkId)

        _state.update {
            when (result) {
                is ConnectionResult.Success -> it.copy(
                    isBusy = false,
                    manualResult = result,
                    bannerMessage = "✅ Запрос выполнен успешно",
                    bannerLevel = BannerLevel.INFO
                )

                is ConnectionResult.Failure -> it.copy(
                    isBusy = false,
                    manualResult = result,
                    bannerMessage = "❌ Сервер недоступен.",
                    bannerLevel = BannerLevel.ERROR
                )
            }
        }
    }

    private suspend fun handleImport(json: String) {
        when (val result = parser.parse(json)) {

            is ImportResult.Success -> {
                networksRepo.replaceAll(result.networks)
                importStateRepo.markImported()

                modeRepo.setAuto()
                AgentForegroundService.start(context)

                _state.update {
                    it.copy(
                        networks = result.networks,
                        importSuccess = true,
                        importError = null,
                        mode = AppMode.AUTO,
                        bannerMessage = "✅ Импорт завершён",
                        bannerLevel = BannerLevel.INFO
                    )
                }
            }

            is ImportResult.Error -> {
                _state.update {
                    it.copy(
                        importError = result.message,
                        importSuccess = false,
                        bannerMessage = "❌ Ошибка импорта: ${result.message}",
                        bannerLevel = BannerLevel.ERROR
                    )
                }
            }
        }
    }
    private var countdownJob: Job? = null

    private fun startAutoCountdown(nextCycleAt: Long) {
        countdownJob?.cancel()

        countdownJob = viewModelScope.launch {
            while (true) {
                val remaining = nextCycleAt - System.currentTimeMillis()

                if (remaining <= 0) {
                    _state.update { it.copy(autoCountdownMs = 0L) }
                    break
                }

                _state.update {
                    it.copy(autoCountdownMs = remaining)
                }

                delay(1_000)
            }
        }
    }

    private fun stopAutoCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _state.update { it.copy(autoCountdownMs = 0L) }
    }

    fun onImportHandled() {
        _state.update { it.copy(requestImportJson = false) }
    }


}
