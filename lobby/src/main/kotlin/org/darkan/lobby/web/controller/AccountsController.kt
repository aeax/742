package org.darkan.lobby.web.controller

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.darkan.core.mongo.collections.Accounts
import org.darkan.core.net.web.AccountCreateRequest
import org.darkan.core.type.Account

private val passwordFields = arrayOf(Account::passwordHash.name, Account::password.name, Account::legacyPass.name)

object AccountsController {
    fun registerRoutes(routing: Routing) {
        routing.route("/accounts") {
            get("/{username}") {
                val username = call.parameters["username"] ?: throw IllegalArgumentException("Invalid username")
                val account = Accounts.find(username)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(JsonObject(Json.encodeToJsonElement(account).jsonObject.filterNot { passwordFields.contains(it.key) }))
            }

            post {
                val req = call.receive<AccountCreateRequest>()
                val account = Accounts.create(req) ?: return@post call.respond(HttpStatusCode.Conflict)
                call.respond(HttpStatusCode.OK, account)
            }
        }
    }
}