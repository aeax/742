package org.darkan.core.net

import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import org.darkan.core.Logger.logWarn
import world.gregs.voidps.buffer.writeByte
import world.gregs.voidps.buffer.writeShort
import world.gregs.voidps.buffer.writeSmart

open class Session(
    private val write: ByteWriteChannel,
    val isaacIn: Isaac,
    private val isaacOut: Isaac?,
    val ip: String
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

    open suspend fun send(opcode: Int, block: suspend ByteWriteChannel.() -> Unit) = send(opcode, -1, FIXED, block)

    open suspend fun send(opcode: Int, size: Int, type: Int, block: suspend ByteWriteChannel.() -> Unit) {
        if (disconnected) return
        try {
            write.header(opcode, type, size, isaacOut)
            block.invoke(write)
        } catch (e: Exception) {
            logWarn("Client error:", e)
            runBlocking { disconnect() }
        }
    }

    private suspend fun ByteWriteChannel.header(opcode: Int, type: Int, size: Int, cipher: Isaac?) {
        if (opcode < 0) return
        if (cipher != null) {
            if (opcode >= 128) {
                writeByte(((opcode shr 8) + 128) + cipher.nextInt())
                writeByte(opcode + cipher.nextInt())
            } else
                writeByte(opcode + cipher.nextInt())
        } else {
            writeSmart(opcode)
        }
        when (type) {
            BYTE -> writeByte(size)
            SHORT -> writeShort(size)
        }
    }

    companion object {
        const val FIXED = 0
        const val BYTE = -1
        const val SHORT = -2

        fun smart(value: Int) = if (value >= 128) 2 else 1

        fun string(value: String?) = (value?.length ?: 0) + 1

        fun bits(bitCount: Int) = (bitCount + 7) / 8

        fun name(displayName: String, responseName: String): Int {
            return 1 + string(displayName) + if (displayName != responseName) string(responseName) else 0
        }
    }
}