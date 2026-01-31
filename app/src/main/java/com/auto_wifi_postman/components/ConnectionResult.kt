package com.auto_wifi_postman.components

sealed class ConnectionResult {
    data object Success : ConnectionResult()
    data class Failure(val reason: String) : ConnectionResult()
}