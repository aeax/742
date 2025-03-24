package org.darkan.core.net

import io.ktor.utils.io.*
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.remaining
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import org.darkan.core.Logger.logWarn
import org.darkan.core.net.prot.Codec
import org.darkan.core.net.prot.ProtSize
import org.darkan.core.net.prot.ServerProt
import world.gregs.voidps.buffer.writeByte
import world.gregs.voidps.buffer.writeSmart

open class Session(
    private val write: ByteWriteChannel,
    val isaacIn: Isaac,
    private val isaacOut: Isaac?,
    val ip: String,
    val codec: Codec,
) {
    enum class State { CONNECTED, LOST_CONNECTION, DISCONNECTED }

    var disconnected: Boolean = false
    private var disconnect: (() -> Unit)? = null
    private var disconnecting: (() -> Unit)? = null
    private var state: State = State.CONNECTED

    fun onDisconnected(block: () -> Unit) {
        disconnect = block
    }

    fun onDisconnecting(block: () -> Unit) {
        disconnecting = block
    }

    suspend fun disconnect(reason: Int) {
        if (disconnected) {
            return
        }
        write.writeByte(reason)
        disconnect()
    }

    suspend fun disconnect() {
        if (disconnected) return
        disconnected = true
        write.flushAndClose()
        state = State.DISCONNECTED
        disconnect?.invoke()
    }

    fun exit() {
        if (state == State.CONNECTED) {
            state = State.LOST_CONNECTION
            disconnecting?.invoke()
        }
    }

    open suspend fun flush() {
        if (disconnected) return
        write.flush()
    }

    open suspend fun send(serverProt: ServerProt, noIsaac: Boolean = false) {
        if (disconnected) return
        try {
            val encoder = codec.serverProts[serverProt::class] ?: return logWarn("Missing ServerProt encoder: ${serverProt::class}")

            when (encoder.size) {
                is ProtSize.Fixed -> {
                    writeOpcode(encoder.opcode, if (noIsaac) null else isaacOut)
                    encoder.encoder?.invoke(serverProt, write)
                }
                ProtSize.VarByte, ProtSize.VarShort -> {
                    val dataChannel = ByteChannel()
                    encoder.encoder?.invoke(serverProt, dataChannel)
                    val packetData = dataChannel.toByteReadPacket()
                    val packetLength = packetData.remaining

                    if (encoder.size == ProtSize.VarByte && packetLength > 255)
                        logWarn("Packet length exceeds VarByte maximum (${packetLength} > 255)")
                    else if (encoder.size == ProtSize.VarShort && packetLength > 65535)
                        logWarn("Packet length exceeds VarShort maximum (${packetLength} > 65535)")

                    writeOpcode(encoder.opcode, if (noIsaac) null else isaacOut)

                    if (encoder.size == ProtSize.VarByte)
                        write.writeByte(packetLength.toByte())
                    else
                        write.writeShort(packetLength.toShort())

                    write.writePacket(packetData)
                }
            }
        } catch (e: Exception) {
            logWarn("Client error:", e)
            runBlocking { disconnect() }
        }
    }

    private suspend fun writeOpcode(opcode: Int, cipher: Isaac?) {
        if (opcode < 0) return
        if (cipher != null) {
            if (opcode >= 128) {
                write.writeByte(((opcode shr 8) + 128) + cipher.nextInt())
                write.writeByte(opcode + cipher.nextInt())
            } else
                write.writeByte(opcode + cipher.nextInt())
        } else
            write.writeSmart(opcode)
    }

    private suspend fun ByteChannel.toByteReadPacket(): Source {
        flush()
        val packet = ByteReadPacket(ByteArray(availableForRead).also { readFully(it) })
        close()
        return packet
    }
}