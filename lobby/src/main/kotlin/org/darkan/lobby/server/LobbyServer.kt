package org.darkan.lobby.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.CancellationException
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.io.EOFException
import kotlinx.io.readByteArray
import kotlinx.io.readUByte
import kotlinx.io.readUShort
import org.darkan.core.EnvVars
import org.darkan.core.Logger.logError
import org.darkan.core.Logger.logInfo
import org.darkan.core.Logger.logTrace
import org.darkan.core.crypt.Crypto
import org.darkan.core.formatPlayerNameForProtocol
import org.darkan.core.generateRandom24ByteArray
import org.darkan.core.mongo.collections.Accounts
import org.darkan.core.net.*
import org.darkan.core.net.prot.Codec
import org.darkan.core.net.prot.UpdateUid192
import org.darkan.core.model.MachineInformation
import org.darkan.lobby.Lobby
import org.darkan.lobby.web.model.AccountCreationSession
import org.darkan.lobby.web.model.LobbyPlayer
import world.gregs.voidps.buffer.*
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.decryptXtea
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class LobbyServer(val js5: JS5Server) {
    private lateinit var job: Job
    private lateinit var dispatcher: ExecutorCoroutineDispatcher

    private lateinit var serverSocket: ServerSocket
    private lateinit var selectorManager: SelectorManager

    private val js5RsaMod = BigInteger(EnvVars.js5RsaModulus)
    private val js5RsaExp = BigInteger(EnvVars.js5RsaExponent)

    private val pendingLogins = ConcurrentHashMap.newKeySet<String>()

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
                    RequestOpcode.ACCOUNT_CREATION -> initAccountCreation(input, output, ip)
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
        val minor = packet.readInt()
        val rsaSize = packet.readUShort().toInt()
        val codec = Codec.get(major) ?: return output.finish(ResponseOpcode.GAME_UPDATE)
        val sensitiveData = ByteReadPacket(RSA.crypt(packet.readByteArray(rsaSize), js5RsaMod, js5RsaExp))
        if (sensitiveData.readUByte().toInt() != 10) return output.finish(ResponseOpcode.BAD_SESSION_ID)
        val isaacKeys = IntArray(4) { sensitiveData.readInt() }
        if (sensitiveData.readLong().toInt() != 0) return output.finish(ResponseOpcode.BAD_SESSION_ID)
        val password = sensitiveData.readRSString()
        val unk1 = sensitiveData.readLong()
        val unk2 = sensitiveData.readLong()
        val xtea = packet.decryptXtea(isaacKeys)
        val stringUsername = xtea.readBoolean()
        val username = (if (stringUsername) xtea.readRSString() else xtea.readLong().toRSString()).formatPlayerNameForProtocol()
        val gameType = xtea.readUByte()
        val language = xtea.readUByte()
        val randomDat = xtea.readByteArray(24)
        val loginServerToken = xtea.readRSString()
        val prefSize = xtea.readUByte().toInt()
        val prefs = IntArray(prefSize)
        for (i in prefs.indices) prefs[i] = xtea.readUByte().toInt()
        val clientKey = xtea.readRSString()
        val unknown1 = xtea.readInt()
        val unknown2 = xtea.readInt()
        val js5ServerToken = xtea.readRSString()

        if (loginServerToken != EnvVars.loginServerToken) {
            logError("Login server token mismatch: ${EnvVars.loginServerToken}, $loginServerToken from $username")
            //return output.finish(ResponseOpcode.GAME_UPDATE)
        }

        if (js5ServerToken != EnvVars.js5ServerToken) {
            logError("JS5 server token mismatch: ${EnvVars.js5ServerToken}, $js5ServerToken from $username")
            //return output.finish(ResponseOpcode.GAME_UPDATE)
        }

        if (clientKey != EnvVars.clientKey) {
            logError("Client key mismatch: ${EnvVars.clientKey}, $clientKey from $username")
            //return output.finish(ResponseOpcode.GAME_UPDATE)
        }

        for (index in 0..<Cache.get().indexCount()) {
            if (index > 35) break
            val crc = Cache.get().indexCrcs()[index].toInt()
            val receivedCRC = xtea.readInt()
            if (crc != receivedCRC && index < 32) {
                logError("CRC mismatch: $crc, $receivedCRC from $username")
                return output.finish(ResponseOpcode.GAME_UPDATE)
            }
        }

        // Remove any existing pending login for this username first (in case of previous disconnect)
        pendingLogins.remove(username)
        // Add to pending logins (this should always succeed now)
        pendingLogins.add(username)
        println("DEBUG: Looking up account with username: '$username'")
        val account = Accounts.find(username) ?: return run {
            println("DEBUG: Account not found for username: '$username'")
            pendingLogins.remove(username)
            output.finish(ResponseOpcode.INVALID_CREDENTIALS)
        }
        println("DEBUG: Found account - username: '${account.username}', email: '${account.email}'")
        if (!account.passwordHash.isEmpty()) {
            if (!Crypto.verifyPasswordArgon2(password, account.passwordHash))
                return run {
                    pendingLogins.remove(username)
                    output.finish(ResponseOpcode.INVALID_CREDENTIALS)
                }
        } else if (account.password != null) {
            if (!Crypto.legacyCompare(password, account.password!!.map { it.toByte() }.toByteArray()))
                return run {
                    pendingLogins.remove(username)
                    output.finish(ResponseOpcode.INVALID_CREDENTIALS)
                }
            // Commenting out automatic password conversion to prevent "password updated" message
            // account.passwordHash = Crypto.hashPasswordArgon2(password)
            // account.password = null
            // Accounts.save(account)
        } else if (account.legacyPass != null) {
            if (!Crypto.gigaLegacyCompare(password, account.legacyPass!!))
                return run {
                    pendingLogins.remove(username)
                    output.finish(ResponseOpcode.INVALID_CREDENTIALS)
                }
            // Commenting out automatic password conversion to prevent "password updated" message
            // account.passwordHash = Crypto.hashPasswordArgon2(password)
            // account.legacyPass = null
            // Accounts.save(account)
        }

        logInfo("Logging in $username")
        pendingLogins.remove(username)
        val session = initSession(output, isaacKeys, ip, codec)
        val lobbyPlayer = LobbyPlayer(session, account)
        Lobby.addLobbyPlayer(lobbyPlayer)
        session.onDisconnected { Lobby.removeLobbyPlayer(username) }
        lobbyPlayer.login(input)
    }

    private suspend fun initAccountCreation(input: ByteReadChannel, output: ByteWriteChannel, ip: String) {
        // Removed the 20 concurrent login limit
        val size = input.readShort().toInt()
        val packet = input.readPacket(size)
        val major = packet.readShort().toInt()
        val minor = packet.readShort().toInt()
        val rsaSize = packet.readUShort().toInt()
        val codec = Codec.get(major) ?: return output.finish(ResponseOpcode.GAME_UPDATE)
        val sensitiveData = ByteReadPacket(RSA.crypt(packet.readByteArray(rsaSize), js5RsaMod, js5RsaExp))
        if (sensitiveData.readUByte().toInt() != 10) return output.finish(ResponseOpcode.BAD_SESSION_ID)
        val isaacKeys = IntArray(4) { sensitiveData.readInt() }

        //read 10 random ints and a random short
        for (i in 1..10)
            sensitiveData.readInt()
        sensitiveData.readUShort()

        val xtea = packet.decryptXtea(isaacKeys)
        val js5ServerToken = xtea.readRSString()
        val affiliate = xtea.readShort()
        val clientParam26 = xtea.readLong()
        val clientParam13 = xtea.readRSString()
        val languageId = xtea.readUByte()
        val gameId = xtea.readUByte()
        val randomDat = xtea.readByteArray(24)
        if (xtea.readBoolean())
            xtea.readRSString() //loginServerToken?
        val machineInfo = MachineInformation.parse(xtea)

//        if (loginServerToken != EnvVars.loginServerToken) {
//            logError("Login server token mismatch: ${EnvVars.loginServerToken}, $loginServerToken from $username")
//            //return output.finish(ResponseOpcode.GAME_UPDATE)
//        }
//
        if (js5ServerToken != EnvVars.js5ServerToken) {
            logError("JS5 server token mismatch: ${EnvVars.js5ServerToken}, $js5ServerToken from $ip")
            //return output.finish(ResponseOpcode.GAME_UPDATE)
        }
//
//        if (clientKey != EnvVars.clientKey) {
//            logError("Client key mismatch: ${EnvVars.clientKey}, $clientKey from $username")
//            //return output.finish(ResponseOpcode.GAME_UPDATE)
//        }

        logInfo("Account creation session started from $ip")
        val session = initSession(output, isaacKeys, ip, codec)
        val accCreationSession = AccountCreationSession(session)
        session.onDisconnected { Lobby.removeAccountCreation(accCreationSession) }
        Lobby.addAccountCreation(accCreationSession)
        output.respond(ResponseOpcode.SUCCESS)
        if (randomDat.any { it != (-1).toByte() })
            session.send(UpdateUid192(generateRandom24ByteArray()))
        session.readPackets(input)
    }

    private fun initSession(write: ByteWriteChannel, isaacKeys: IntArray, hostname: String, codec: Codec): Session {
        val inCipher = Isaac(isaacKeys)
        for (i in isaacKeys.indices)
            isaacKeys[i] += 50
        val outCipher = Isaac(isaacKeys)
        return Session(write, inCipher, outCipher, hostname, codec)
    }
}