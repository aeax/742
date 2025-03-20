package org.darkan.lobby.web

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.RequestValidation
import kotlinx.serialization.json.Json
import org.darkan.core.net.web.installCoreValidators
import org.darkan.core.net.web.installExceptions

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            coerceInputValues = true
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            explicitNulls = false
            classDiscriminator = "type"
        })
    }

    installExceptions()
    install(RequestValidation) {
        installCoreValidators()
    }

    configureRouting()
}