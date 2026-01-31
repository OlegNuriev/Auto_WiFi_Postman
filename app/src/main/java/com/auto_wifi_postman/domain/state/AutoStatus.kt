package com.auto_wifi_postman.domain.state

data class AutoStatus(
    val lastCycleTime: Long? = null,
    val nextCycleInMs: Long? = null
)
