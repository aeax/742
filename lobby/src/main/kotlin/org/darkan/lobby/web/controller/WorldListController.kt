package org.darkan.lobby.web.controller

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.darkan.core.worldlist.World
import org.darkan.core.worldlist.WorldMetadata
import org.darkan.lobby.Lobby

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
    }
}