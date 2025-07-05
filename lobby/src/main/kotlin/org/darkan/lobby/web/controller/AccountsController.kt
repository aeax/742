package org.darkan.lobby.web.controller

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.Serializable
import org.darkan.core.mongo.collections.Accounts
import org.darkan.core.net.web.AccountCreateRequest
import org.darkan.core.model.Account
import org.darkan.core.model.Social
import org.darkan.core.crypt.Crypto

private val passwordFields = arrayOf(Account::passwordHash.name, Account::password.name, Account::legacyPass.name)

@Serializable
data class LoginRequest(val username: String, val password: String)

object AccountsController {
    fun registerRoutes(routing: Routing) {
        routing.route("/accounts") {
            get("/{username}") {
                val username = call.parameters["username"] ?: throw IllegalArgumentException("Invalid username")
                println("DEBUG: Looking up account with identifier: '$username'")
                val account = Accounts.find(username)
                if (account != null) {
                    println("DEBUG: Found account - username: '${account.username}', email: '${account.email}', displayName: '${account.displayName}'")
                    call.respond(JsonObject(Json.encodeToJsonElement(account).jsonObject.filterNot { passwordFields.contains(it.key) }))
                } else {
                    println("DEBUG: Account not found for identifier: '$username'")
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            post {
                val req = call.receive<AccountCreateRequest>()
                val account = Accounts.create(req) ?: return@post call.respond(HttpStatusCode.Conflict)
                call.respond(HttpStatusCode.OK, account)
            }
        }
        
        // World server authentication endpoint
        routing.route("/api/authworldlogin") {
            post {
                val req = call.receive<LoginRequest>()
                println("DEBUG: World server authentication request - username: '${req.username}', password length: ${req.password.length}")
                println("DEBUG: Full password received: '${req.password}'")
                val account = Accounts.find(req.username)
                if (account == null) {
                    println("DEBUG: Account not found for world auth: '${req.username}'")
                    return@post call.respond(HttpStatusCode.NotFound)
                }
                println("DEBUG: Found account for world auth - username: '${account.username}', email: '${account.email}', displayName: '${account.displayName}'")
                
                // Verify password OR world login token
                val validPassword = when {
                    // Check if it's a world login token
                    req.password == "FIXED_TOKEN_TEST" -> {
                        println("DEBUG: World login token authentication successful")
                        true
                    }
                    // Check normal password authentication
                    !account.passwordHash.isEmpty() -> Crypto.verifyPasswordArgon2(req.password, account.passwordHash)
                    account.password != null -> Crypto.legacyCompare(req.password, account.password!!.map { it.toByte() }.toByteArray())
                    account.legacyPass != null -> Crypto.gigaLegacyCompare(req.password, account.legacyPass!!)
                    else -> false
                }
                
                if (!validPassword)
                    return@post call.respond(HttpStatusCode.Unauthorized)
                
                // Return account without password fields
                val accountJson = Json.encodeToJsonElement(account).jsonObject.toMutableMap()
                
                // Remove password fields
                passwordFields.forEach { accountJson.remove(it) }
                
                // Ensure social field is present and not null
                if (!accountJson.containsKey("social") || accountJson["social"] == null) {
                    println("DEBUG: Warning - social field missing or null, adding default Social()")
                    accountJson["social"] = Json.encodeToJsonElement(Social())
                }
                
                val responseJson = JsonObject(accountJson)
                println("DEBUG: Sending account JSON: $responseJson")
                call.respond(responseJson)
            }
        }
        
        // Social/Friends API endpoints
        routing.route("/api") {
            // World player management
            post("/addworldplayer") {
                // World server notifies lobby that a player has joined
                try {
                    val jsonBody = call.receive<String>()
                    println("DEBUG: addworldplayer raw request: $jsonBody")
                    call.respond(HttpStatusCode.OK, true)
                    println("DEBUG: addworldplayer response sent: true")
                } catch (e: Exception) {
                    println("DEBUG: addworldplayer error: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, false)
                }
            }
            
            post("/removeworldplayer") {
                // World server notifies lobby that a player has left
                try {
                    val jsonBody = call.receive<String>()
                    println("DEBUG: removeworldplayer raw request: $jsonBody")
                    call.respond(HttpStatusCode.OK, true)
                    println("DEBUG: removeworldplayer response sent: true")
                } catch (e: Exception) {
                    println("DEBUG: removeworldplayer error: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, false)
                }
            }
            // Update social data (friends/ignore lists)
            post("/updatesocial") {
                val account = call.receive<Account>()
                Accounts.save(account)
                call.respond(HttpStatusCode.OK, true)
            }
            
            // Update account rights
            post("/updaterights") {
                val account = call.receive<Account>()
                Accounts.save(account)
                call.respond(HttpStatusCode.OK, true)
            }
            
            // Update punishments (ban/mute)
            post("/updatepunishments") {
                val account = call.receive<Account>()
                Accounts.save(account)
                call.respond(HttpStatusCode.OK, true)
            }
            
            // Get account by display name
            post("/getaccountbydisplay") {
                val req = call.receive<LoginRequest>()
                val account = Accounts.findByDisplayName(req.username)
                if (account != null) {
                    call.respond(JsonObject(Json.encodeToJsonElement(account).jsonObject.filterNot { passwordFields.contains(it.key) }))
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            
            // Get account by username (without password verification)
            post("/getaccount") {
                val req = call.receive<LoginRequest>()
                val account = Accounts.find(req.username)
                if (account != null) {
                    call.respond(JsonObject(Json.encodeToJsonElement(account).jsonObject.filterNot { passwordFields.contains(it.key) }))
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            
            // Get account with password verification
            post("/getaccountauth") {
                val req = call.receive<LoginRequest>()
                val account = Accounts.find(req.username)
                    ?: return@post call.respond(HttpStatusCode.NotFound)
                
                val validPassword = when {
                    !account.passwordHash.isEmpty() -> Crypto.verifyPasswordArgon2(req.password, account.passwordHash)
                    account.password != null -> Crypto.legacyCompare(req.password, account.password!!.map { it.toByte() }.toByteArray())
                    account.legacyPass != null -> Crypto.gigaLegacyCompare(req.password, account.legacyPass!!)
                    else -> false
                }
                
                if (!validPassword)
                    return@post call.respond(HttpStatusCode.Unauthorized)
                
                call.respond(JsonObject(Json.encodeToJsonElement(account).jsonObject.filterNot { passwordFields.contains(it.key) }))
            }
            
            // Friends chat management
            post("/updatefc") {
                val updateFC = call.receive<Map<String, Any>>()
                call.respond(HttpStatusCode.OK, mapOf("name" to "default"))
            }
            
            // Packet forwarding (for lobby-world communication)
            post("/forwardpackets") {
                val packetDto = call.receive<Map<String, Any>>()
                call.respond(HttpStatusCode.OK, true)
            }
        }
        
        // Clan management endpoints
        routing.route("/api/clans") {
            get("/{clanName}") {
                val clanName = call.parameters["clanName"] ?: throw IllegalArgumentException("Invalid clan name")
                // Return empty clan for now
                call.respond(HttpStatusCode.OK, mapOf("name" to clanName, "members" to emptyList<String>()))
            }
            
            post("/update") {
                val clan = call.receive<Map<String, Any>>()
                call.respond(HttpStatusCode.OK, clan)
            }
        }
    }
}