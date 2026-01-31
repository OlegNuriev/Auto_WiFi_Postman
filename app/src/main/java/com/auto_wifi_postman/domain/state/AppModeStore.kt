package com.auto_wifi_postman.domain.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AppModeStore {
    private val _mode = MutableStateFlow(AppMode.AUTO)
    val mode: StateFlow<AppMode> = _mode

    fun set(mode: AppMode) {
        _mode.value = mode
    }
}
