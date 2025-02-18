package org.darkan.lobby.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.CancellationException
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.io.readByteArray
import kotlinx.io.readUByte
import kotlinx.io.readUShort
import okio.EOFException
import org.darkan.core.EnvVars
import org.darkan.core.Logger.logError
import org.darkan.core.Logger.logInfo
import org.darkan.core.Logger.logTrace
import org.darkan.core.net.JS5Server
import org.darkan.core.net.RequestOpcode
import org.darkan.core.net.ResponseOpcode
import org.darkan.core.net.prot.Codec
import world.gregs.voidps.buffer.finish
import world.gregs.voidps.buffer.readString
import world.gregs.voidps.buffer.respond
import world.gregs.voidps.cache.secure.RSA
import java.math.BigInteger
import java.util.concurrent.Executors

class LobbyServer(val js5: JS5Server) {
    private lateinit var job: Job
    private lateinit var dispatcher: ExecutorCoroutineDispatcher

    private lateinit var serverSocket: ServerSocket
    private lateinit var selectorManager: SelectorManager

    private val js5RsaMod = BigInteger(EnvVars.js5RsaModulus)
    private val js5RsaExp = BigInteger(EnvVars.js5RsaExponent)

    suspend fun start(): Job {
        val executor = Executors.newCachedThreadPool()
        dispatcher = executor.asCoroutineDispatcher()
        selectorManager = ActorSelectorManager(dispatcher)
        val scope = CoroutineScope(dispatcher)
        logInfo("Starting lobby server...")
        serverSocket = aSocket(selectorManager).tcp().bind("0.0.0.0", EnvVars.lobbyPort) { reuseAddress = true }
        logInfo("Started lobby server...")
        job = scope.launch {
            try {
                supervisorScope {
                    logInfo("Lobby server started on port ${EnvVars.lobbyPort}")
                    while (isActive) {
                        val socket = serverSocket.accept()
                        logInfo("Client connected: ${socket.remoteAddress.toJavaAddress().hostname}")
                        connectClient(socket)
                    }
                }
            } catch (_: CancellationException) {
                logInfo("Lobby server stopping...")
            } catch (e: Exception) {
                logError("Error in Lobby server", e)
            }
        }
        return job
    }

    fun stop() {
        try {
            job.cancel()
            dispatcher.close()
            if (::serverSocket.isInitialized)
                serverSocket.close()
            if (::selectorManager.isInitialized)
                selectorManager.close()
        } catch (e: Exception) {
            logError("Error stopping Lobby server", e)
            throw e
        }
    }

    private fun CoroutineScope.connectClient(socket: Socket) = launch(dispatcher + CoroutineExceptionHandler { context, throwable -> logTrace("Error connecting client: ${throwable.message}") }) {
        try {
            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = false)
            val ip = socket.remoteAddress.toJavaAddress().hostname

            try {
                when (val reqOpcode = input.readByte().toInt()) {
                    RequestOpcode.CONNECT_LOGIN -> init(input, output, ip)
                    RequestOpcode.JS5_INIT -> js5.init(input, output, ip)
                    else -> {
                        logTrace("Invalid request opcode: $reqOpcode")
                        output.finish(ResponseOpcode.INVALID_LOGIN_SERVER)
                    }
                }
            } finally {
                socket.close()
            }
        } catch (e: Exception) {
            if (e !is EOFException)
                logError("Error handling client connection", e)
        } finally {
            socket.close()
        }
    }

    private suspend fun init(input: ByteReadChannel, output: ByteWriteChannel, ip: String) {
        output.respond(ResponseOpcode.JS5_SYNC)
        val opcode = input.readByte().toInt()
        if (opcode != RequestOpcode.LOBBY) return output.finish(ResponseOpcode.LOGIN_SERVER_REJECTED_SESSION)
        val size = input.readShort().toInt()
        val packet = input.readPacket(size)
        val major = packet.readInt()
        val patch = packet.readInt()
        val rsaSize = packet.readUShort().toInt()
        val codec = Codec.get(major) ?: return output.finish(ResponseOpcode.GAME_UPDATE)
        val sensitiveData = ByteReadPacket(RSA.crypt(packet.readByteArray(rsaSize), js5RsaMod, js5RsaExp))
        if (sensitiveData.readUByte().toInt() != 10) return output.finish(ResponseOpcode.BAD_SESSION_ID)
        val isaacKeys = IntArray(4) { sensitiveData.readInt() }
        if (sensitiveData.readLong().toInt() != 0) return output.finish(ResponseOpcode.BAD_SESSION_ID)
        val password = sensitiveData.readString()
        val unk1 = sensitiveData.readLong()
        val unk2 = sensitiveData.readLong()
        println("$opcode $major $patch $rsaSize $password $unk1 $unk2 ${isaacKeys.contentToString()}")
    }
}