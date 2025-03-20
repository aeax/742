package org.darkan.lobby.web

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.darkan.core.net.web.installCoreValidators
import org.darkan.core.net.web.installExceptions
import org.darkan.lobby.web.controller.AccountsController

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(buildJsonObject {
                put("status", "OK")
                put("version", this.javaClass.`package`.implementationVersion ?: "dev")
                put("timestamp", System.currentTimeMillis())
            })
        }

        AccountsController.registerRoutes(this)
    }
}