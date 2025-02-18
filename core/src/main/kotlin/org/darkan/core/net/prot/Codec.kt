package org.darkan.core.net.prot

import io.ktor.utils.io.*
import kotlin.reflect.KClass

class Codec {
    val serverProts = mutableMapOf<KClass<out ServerProt>, ServerProtCodec>()
    val clientProtsByOpcode = mutableMapOf<Int, ClientProtCodec<*>>()

    data class ServerProtCodec(
        val opcode: Int,
        val size: ProtSize,
        val encoder: (suspend ServerProt.(ByteWriteChannel) -> Unit)?
    )

    data class ClientProtCodec<T : ClientProt>(
        val size: ProtSize,
        val decoder: (suspend ByteReadChannel.(Int) -> T)?
    )

    internal inline fun <reified T : ServerProt> serverProt(opcode: Int, size: ProtSize = ProtSize.Fixed(0), noinline encoder: (suspend T.(ByteWriteChannel) -> Unit)? = null) {
        serverProts[T::class] = ServerProtCodec(
            opcode = opcode,
            size = size,
            encoder = encoder?.let { { output -> (this as T).it(output) } }
        )
    }

    internal inline fun <reified T : ClientProt> clientProt(opcodes: IntArray, size: ProtSize = ProtSize.Fixed(0), noinline decoder: (suspend ByteReadChannel.(Int) -> T)? = null) {
        val codec = ClientProtCodec(size, decoder)
        opcodes.forEach { opcode -> clientProtsByOpcode[opcode] = codec }
    }

    internal inline fun <reified T : ClientProt> clientProt(opcode: Int, size: ProtSize = ProtSize.Fixed(0), noinline decoder: (suspend ByteReadChannel.() -> T)? = null) {
        clientProt<T>(
            opcodes = intArrayOf(opcode),
            size = size,
            decoder = decoder?.let { { _ -> this.it() } }
        )
    }

    suspend inline fun <reified T : ClientProt> decodeClientProt(opcode: Int, channel: ByteReadChannel): T? {
        val codec = clientProtsByOpcode[opcode] ?: return null
        @Suppress("UNCHECKED_CAST")
        return when {
            codec.decoder != null -> codec.decoder.invoke(channel, opcode) as T
            codec.size is ProtSize.Fixed && codec.size.length == 0 ->
                try {
                    T::class.constructors.firstOrNull { it.parameters.isEmpty() }?.call()
                        ?: error("No empty constructor found for packet type: ${T::class}")
                } catch (e: Exception) {
                    error("Failed to create instance of empty packet ${T::class}: ${e.message}")
                }
            else -> error("No decoder provided for non-empty packet type: ${T::class}")
        }
    }

    suspend fun encodeServerProt(prot: ServerProt, output: ByteWriteChannel) {
        val codec = serverProts[prot::class] ?: error("Unknown packet type: ${prot::class}")
        codec.encoder?.invoke(prot, output)
    }

    companion object {
        private val codecs = mutableMapOf<Int, Codec>()

        fun register(revision: Int, init: Codec.() -> Unit): Codec {
            val codec = Codec().apply(init)
            return codecs.getOrPut(revision) { codec }
        }

        fun get(revision: Int) = codecs[revision]
    }
}