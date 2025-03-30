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
import org.darkan.core.net.prot.handler.PacketHandlers
import org.darkan.core.worldlist.Country
import org.darkan.core.worldlist.World
import org.darkan.core.worldlist.WorldList
import org.darkan.core.worldlist.WorldMetadata
import org.darkan.lobby.server.LobbyServer
import org.darkan.lobby.web.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.file.FileProvider
import world.gregs.voidps.cache.file.prefetchKeys
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

    val worldList = run {
        val worlds = WorldList(300)
        worlds.put(1, World(WorldMetadata(1, "prod.darkan.org", 43595, "Darkan Prod", Country.USA, false, true, true, false, false)))
        worlds.put(2, World(WorldMetadata(2, "dev.darkan.org", 43595, "Darkan Dev", Country.USA, false, true, true, false, false)))
        worlds.put(3, World(WorldMetadata(3, "google.com", 43595, "Google", Country.USA, false, true, true, false, false)))
        worlds.put(4, World(WorldMetadata(4, "1.1.1.1", 43595, "Cloudflare", Country.USA, false, true, true, false, false)))
        worlds.put(5, World(WorldMetadata(5, "runescape.com", 43595, "RuneScape", Country.USA, false, true, true, false, false)))
        worlds.put(6, World(WorldMetadata(6, "world2.runescape.com", 43595, "RuneScape (East Coast)", Country.USA, false, true, true, false, false)))
        worlds.put(7, World(WorldMetadata(7, "world14.runescape.com", 43595, "RuneScape (West Coast)", Country.USA, false, true, true, false, false)))
        worlds.put(8, World(WorldMetadata(8, "world15.runescape.com", 43595, "RuneScape (Australia)", Country.USA, false, true, true, false, false)))
        worlds.put(9, World(WorldMetadata(9, "world19.runescape.com", 43595, "RuneScape (Netherlands)", Country.USA, false, true, true, false, false)))
        worlds.put(10, World(WorldMetadata(10, "world28.runescape.com", 43595, "RuneScape (Poland)", Country.USA, false, true, true, false, false)))
        worlds.put(300, World(WorldMetadata(300, "localhost", 43595, "localhost:43595", Country.USA, false, true, true, false, false)))
        worlds
    }
    private val accountCreationSessions = ConcurrentHashMap<String, Session>()
    private val lobbyPlayers = ConcurrentHashMap<String, Session>()

    fun start() {
        logInfo("Starting application services...")
        cache = Cache.get()
        cacheProvider = FileProvider.load(cache)
        js5Server = JS5Server(cacheProvider, prefetchKeys(cache))
        PacketHandlers.loadHandlersFromPackage("org.darkan.lobby.server.packet")
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

    fun removeLobbyPlayer(username: String) = lobbyPlayers.remove(username)
}