package com.auto_wifi_postman.data.importer

import com.auto_wifi_postman.domain.model.KnownNetwork

sealed class ImportResult {
    data class Success(val networks: List<KnownNetwork>) : ImportResult()
    data class Error(val message: String) : ImportResult()
}
