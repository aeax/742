package org.darkan.lobby.web.controller

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.darkan.core.worldlist.World
import org.darkan.core.worldlist.WorldMetadata
import org.darkan.core.worldlist.Country
import org.darkan.lobby.Lobby
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

// Data class to match the world server's format
@Serializable
data class WorldServerInfo(
    val number: Int,
    val ipAddress: String,
    val port: Int,
    val activity: String,
    val country: Int,
    val quickChat: Boolean,
    val lootShare: Boolean,
    val members: Boolean,
    val pvp: Boolean,
    val highlight: Boolean
)

object WorldListController {
    fun registerRoutes(routing: Routing) {
        routing.route("/worldlist") {
            get { call.respond(Lobby.worldList.getWorldArray()) }

            post {
                val req = call.receive<WorldMetadata>()
                Lobby.worldList.put(World(req))
                call.respond(HttpStatusCode.Created)
            }
        }
        
        // Compatibility endpoint for world server
        routing.route("/api/addworld") {
            post {
                try {
                    // Parse the world server format
                    val worldServerInfo = call.receive<WorldServerInfo>()
                    println("Received world registration: $worldServerInfo")
                    
                    // Convert to WorldMetadata format
                    val worldMetadata = WorldMetadata(
                        number = worldServerInfo.number,
                        ipAddress = worldServerInfo.ipAddress,
                        port = worldServerInfo.port,
                        activity = worldServerInfo.activity,
                        country = Country.entries.getOrNull(worldServerInfo.country) ?: Country.USA,
                        quickchat = worldServerInfo.quickChat,
                        lootShare = worldServerInfo.lootShare,
                        members = worldServerInfo.members,
                        pvp = worldServerInfo.pvp,
                        highlighted = worldServerInfo.highlight
                    )
                    
                    // Add the world to the lobby's world list
                    Lobby.worldList.put(World(worldMetadata))
                    println("Added world ${worldServerInfo.number} to world list")
                    
                    // Return just the boolean value true as JSON
                    call.respondText("true", ContentType.Application.Json)
                } catch (e: Exception) {
                    println("Error in /api/addworld: ${e.message}")
                    e.printStackTrace()
                    call.respondText("false", ContentType.Application.Json)
                }
            }
        }
    }
}