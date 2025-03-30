package org.darkan.core.net.prot

import io.ktor.utils.io.*
import kotlinx.io.Source
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

class Codec {
    val serverProts = mutableMapOf<KClass<out ServerProt>, ServerProtCodec>()
    val clientProtsByOpcode = mutableMapOf<Int, ClientProtCodec<*>>()
    private val opcodeToClassMap = mutableMapOf<Int, KClass<out ClientProt>>()

    data class ServerProtCodec(
        val opcode: Int,
        val size: ProtSize,
        val encoder: (suspend ServerProt.(ByteWriteChannel) -> Unit)?
    )

    data class ClientProtCodec<T : ClientProt>(
        val size: ProtSize,
        val decoder: (suspend Source.(Int) -> T)?,
        val protClass: KClass<T>
    )

    internal inline fun <reified T : ServerProt> serverProt(opcode: Int, size: ProtSize = ProtSize.Fixed(0), noinline encoder: (suspend T.(ByteWriteChannel) -> Unit)? = null) {
        serverProts[T::class] = ServerProtCodec(
            opcode = opcode,
            size = size,
            encoder = encoder?.let { { output -> (this as T).it(output) } }
        )
    }

    internal inline fun <reified T : ServerProt> serverProt(opcode: Int, size: Int, noinline encoder: (suspend T.(ByteWriteChannel) -> Unit)? = null) {
        serverProts[T::class] = ServerProtCodec(
            opcode = opcode,
            size = ProtSize.Fixed(size),
            encoder = encoder?.let { { output -> (this as T).it(output) } }
        )
    }

    internal inline fun <reified T : ClientProt> clientProt(opcodes: IntArray, size: ProtSize = ProtSize.Fixed(0), noinline decoder: (suspend Source.(Int) -> T)? = null) {
        val codec = ClientProtCodec(size, decoder, T::class)
        opcodes.forEach { opcode ->
            clientProtsByOpcode[opcode] = codec
            opcodeToClassMap[opcode] = T::class
        }
    }

    internal inline fun <reified T : ClientProt> clientProt(opcodes: IntArray, size: Int, noinline decoder: (suspend Source.(Int) -> T)? = null) {
        val codec = ClientProtCodec(ProtSize.Fixed(size), decoder, T::class)
        opcodes.forEach { opcode ->
            clientProtsByOpcode[opcode] = codec
            opcodeToClassMap[opcode] = T::class
        }
    }

    internal inline fun <reified T : ClientProt> clientProt(opcode: Int, size: ProtSize = ProtSize.Fixed(0), noinline decoder: (suspend Source.() -> T)? = null) {
        clientProt<T>(
            opcodes = intArrayOf(opcode),
            size = size,
            decoder = decoder?.let { { _ -> this.it() } }
        )
    }

    internal inline fun <reified T : ClientProt> clientProt(opcode: Int, size: Int, noinline decoder: (suspend Source.() -> T)? = null) {
        clientProt<T>(
            opcodes = intArrayOf(opcode),
            size = ProtSize.Fixed(size),
            decoder = decoder?.let { { _ -> this.it() } }
        )
    }

    suspend inline fun <reified T : ClientProt> decodeClientProt(opcode: Int, channel: Source): T? {
        val codec = clientProtsByOpcode[opcode] ?: return null
        @Suppress("UNCHECKED_CAST")
        return when {
            codec.decoder != null -> codec.decoder.invoke(channel, opcode) as T
            codec.size is ProtSize.Fixed && codec.size.length == 0 ->
                try {
                    T::class.constructors.firstOrNull { it.parameters.isEmpty() }?.call() ?: error("No empty constructor found for packet type: ${T::class}")
                } catch (e: Exception) {
                    error("Failed to create instance of empty packet ${T::class}: ${e.message}")
                }
            else -> error("No decoder provided for non-empty packet type: ${T::class}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : ClientProt> createInstanceForOpcode(opcode: Int): T? {
        val protClass = opcodeToClassMap[opcode] ?: return null

        return try {
            if (protClass.isValue) {
                val constructor = protClass.primaryConstructor
                return if (constructor != null) createValueClassInstance(constructor) as T else null
            } else {
                val constructor = protClass.constructors.firstOrNull { it.parameters.isEmpty() }
                return constructor?.call() as T
            }
        } catch (e: Exception) {
            return null
        }
    }

    private fun <T : Any> createValueClassInstance(constructor: KFunction<T>): T {
        val args = mutableMapOf<kotlin.reflect.KParameter, Any?>()
        for (param in constructor.parameters) {
            when (param.type.classifier) {
                Int::class -> args[param] = 0
                String::class -> args[param] = ""
                Boolean::class -> args[param] = false
                else -> args[param] = null
            }
        }
        return constructor.callBy(args)
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