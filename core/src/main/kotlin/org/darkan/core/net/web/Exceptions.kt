package org.darkan.core.net.web

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.installExceptions() {
    install(StatusPages) {
        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause.message ?: "Resource conflict")
        }
    }
}

class ConflictException(message: String) : RuntimeException(message)