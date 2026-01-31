package com.auto_wifi_postman.domain.validation

import com.auto_wifi_postman.domain.model.KnownNetwork

object KnownNetworkValidator {

    fun validate(network: KnownNetwork): List<String> {
        val errors = mutableListOf<String>()

        if (network.id.isBlank())
            errors += "id is blank"

        if (network.ssid.isBlank())
            errors += "ssid is blank"

        if (!network.baseUrl.startsWith("http"))
            errors += "baseUrl must start with http/https"

        if (!network.updateEndpoint.startsWith("/"))
            errors += "updateEndpoint must start with /"

        if (network.timeoutMs <= 0)
            errors += "timeoutMs must be > 0"

        if (network.retries <= 0)
            errors += "retries must be > 0"

        return errors
    }
}
