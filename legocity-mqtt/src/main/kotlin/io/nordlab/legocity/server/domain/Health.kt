package io.nordlab.legocity.server.domain

import io.micronaut.serde.annotation.Serdeable
import java.time.ZonedDateTime

@Serdeable
class Health {
    companion object InitHealth {
        val startupTime: ZonedDateTime = ZonedDateTime.now()
        val version: String = Health::class.java.`package`.implementationVersion ?: "development"
        val service: String = Health::class.java.`package`.implementationTitle ?: "legocity-server"
    }

    val runningSince: ZonedDateTime = InitHealth::startupTime.get()
    val version = InitHealth::version.get()
    val service = InitHealth::service.get()
    val now: ZonedDateTime = ZonedDateTime.now()
}