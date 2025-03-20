package org.darkan.lobby

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import org.darkan.core.EnvVars
import org.darkan.core.Logger
import org.darkan.core.Logger.logInfo
import org.darkan.core.net.JS5Server
import org.darkan.core.net.Session
import org.darkan.lobby.server.LobbyServer
import org.darkan.lobby.web.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.file.FileProvider
import world.gregs.voidps.cache.secure.Huffman
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

object Lobby : CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job

    private lateinit var cache: Cache
    private lateinit var cacheProvider: FileProvider
    private lateinit var js5Server: JS5Server
    private lateinit var httpServer: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>
    private lateinit var lobbyServer: LobbyServer

    private val accountCreationSessions = ConcurrentHashMap<String, Session>()
    private val lobbyPlayers = ConcurrentHashMap<String, Session>()

    fun start() {
        logInfo("Starting application services...")
        cache = Cache.get()
        cacheProvider = FileProvider.load(cache)
        js5Server = JS5Server(cacheProvider)
        startLobbyAPI()
        runBlocking {
            startLobbyServer()
        }

        logInfo("All services started successfully")
    }

    suspend fun stop() {
        logInfo("Stopping application services...")

        try {
            if (::lobbyServer.isInitialized) {
                lobbyServer.stop()
            }

            if (::httpServer.isInitialized) {
                httpServer.stop(1000, 2000)
            }

            job.cancelAndJoin()
            logInfo("All services stopped successfully")
        } catch (e: Exception) {
            Logger.logError("Error during shutdown", e)
            throw e
        }
    }

    private fun startLobbyAPI() {
        httpServer = embeddedServer(Netty, port = EnvVars.lobbyApiPort, module = Application::module)
        httpServer.start(wait = false)
        logInfo("API server started on port ${EnvVars.lobbyApiPort}")
    }

    private suspend fun startLobbyServer() {
        lobbyServer = LobbyServer(js5Server)
        lobbyServer.start()
    }
}