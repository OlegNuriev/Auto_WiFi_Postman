package com.auto_wifi_postman.domain.state

enum class AgentState {
    IDLE,
    SCANNING,
    FOUND_NETWORKS,
    CONNECTING,
    HTTP_CHECK,
    DISCONNECT,
    SLEEP

}
