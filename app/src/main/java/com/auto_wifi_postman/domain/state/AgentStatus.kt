package com.auto_wifi_postman.domain.state

data class AgentStatus(
    val lastCycleAt: Long = 0L,
    val nextCycleAt: Long = 0L,
    val isRunning: Boolean = false,
    val lastSuccess: Boolean? = null
)