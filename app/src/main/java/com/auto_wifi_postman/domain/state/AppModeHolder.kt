package com.auto_wifi_postman.domain.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppModeHolder @Inject constructor() {

    private val _mode = MutableStateFlow(AppMode.AUTO)
    val mode: StateFlow<AppMode> = _mode

    fun switchToAuto() {
        _mode.value = AppMode.AUTO
    }

    fun switchToManual() {
        _mode.value = AppMode.MANUAL
    }
}
