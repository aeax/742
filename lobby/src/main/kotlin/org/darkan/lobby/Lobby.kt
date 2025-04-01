package org.darkan.lobby

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import org.darkan.core.EnvVars
import org.darkan.core.Logger
import org.darkan.core.Logger.logInfo
import org.darkan.core.engine.EngineLoop
import org.darkan.core.formatNumber
import org.darkan.core.net.JS5Server
import org.darkan.core.net.Session
import org.darkan.core.net.prot.handler.PacketHandlers
import org.darkan.core.net.web.DiscordWebhook
import org.darkan.core.ticksToTimeString
import org.darkan.core.worldlist.World
import org.darkan.core.worldlist.WorldList
import org.darkan.lobby.server.LobbyServer
import org.darkan.lobby.web.model.LobbyPlayer
import org.darkan.lobby.web.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.file.FileProvider
import world.gregs.voidps.cache.file.prefetchKeys
import java.lang.management.ManagementFactory
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

    val worldList = WorldList(300)
    private val accountCreationSessions = ConcurrentHashMap<String, Session>()
    private val lobbyPlayers = ConcurrentHashMap<String, LobbyPlayer>()
    private lateinit var lobbyEngineLoop: EngineLoop

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
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
        lobbyEngineLoop = EngineLoop(
            arrayOf(
                processInput,
                flushPlayers
            ),
            ::reportTickConcern
        )
        lobbyEngineLoop.start(CoroutineScope(newSingleThreadContext("Lobby engine loop")))
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
    fun addLobbyPlayer(lobbyPlayer: LobbyPlayer) = lobbyPlayers.put(lobbyPlayer.account.username, lobbyPlayer)

    val processInput = Runnable {
        runBlocking {
            lobbyPlayers.values.forEach { it.handleDecodedPackets() }
        }
    }

    val flushPlayers = Runnable {
        runBlocking {
            lobbyPlayers.values.forEach { it.session.flush() }
            accountCreationSessions.values.forEach { it.flush() }
        }
    }

    fun reportTickConcern(actualTime: Long, stepTimes: Map<String, Long>) {
        val memoryBean = ManagementFactory.getMemoryMXBean()
        val heap = memoryBean.heapMemoryUsage
        val nonHeap = memoryBean.nonHeapMemoryUsage

        val heapUsed = heap.used / 1024 / 1024
        val nonHeapUsed = nonHeap.used / 1024 / 1024
        val totalUsed = heapUsed + nonHeapUsed
        val maxMemory = (heap.max + nonHeap.max) / 1024 / 1024
        val usagePercent = (totalUsed.toDouble() / maxMemory) * 100

        val content = buildString {
            appendLine("__**Tick concern**__")
            appendLine("__**Lobby Data**__")
            appendLine("```")
            appendLine("Lobby${EnvVars.serverName} ${if (EnvVars.debug) "(debug)" else ""}")
            appendLine("Uptime: ${lobbyEngineLoop.uptimeTicks.ticksToTimeString()}")
            appendLine("Account creation sessions: ${accountCreationSessions.size.formatNumber()}")
            appendLine("Player sessions: ${lobbyPlayers.size.formatNumber()}")
            appendLine("```")
            appendLine("__**Tick Time: ${actualTime}ms (min: ${lobbyEngineLoop.lowestMillis.formatNumber()}ms avg: ${lobbyEngineLoop.averageMillis.formatNumber()}ms max: ${lobbyEngineLoop.highestMillis.formatNumber()}ms)**__")
            appendLine("```")
            for (key in stepTimes.keys)
                appendLine("$key: ${stepTimes[key]}")
            appendLine("```")
            appendLine("__**JVM Stats:**__")
            appendLine("```")
            appendLine("Total JVM memory usage: ${totalUsed.formatNumber()}mb/${maxMemory.formatNumber()}mb (${usagePercent.formatNumber()}%)")
            appendLine("```")
        }
        DiscordWebhook.sendStaffMessage(content)
    }
}