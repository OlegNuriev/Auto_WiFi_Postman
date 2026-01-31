package com.auto_wifi_postman.data.repository

import com.auto_wifi_postman.domain.state.AgentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentStatusRepository @Inject constructor() {

    private val _status = MutableStateFlow<AgentStatus?>(null)
    val status = _status.asStateFlow()

    fun onCycleStarted() {
        _status.update { current ->
            (current ?: AgentStatus()).copy(
                isRunning = true
            )
        }
    }

    fun onCycleFinished(
        success: Boolean,
        nextCycleAt: Long
    ) {
        _status.update { current ->
            (current ?: AgentStatus()).copy(
                lastCycleAt = System.currentTimeMillis(),
                nextCycleAt = nextCycleAt,
                isRunning = false,
                lastSuccess = success
            )
        }
    }
}
