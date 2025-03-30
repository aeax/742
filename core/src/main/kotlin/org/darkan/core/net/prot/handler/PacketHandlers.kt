package org.darkan.core.net.prot.handler

import org.darkan.core.Logger.logInfo
import org.darkan.core.getClasses
import org.darkan.core.net.prot.ClientProt
import java.lang.reflect.ParameterizedType

interface PacketHandler<T, K : ClientProt> {
    suspend fun handle(player: T, packet: K)
}

object PacketHandlers {
    private val PACKET_HANDLERS = mutableMapOf<Class<out ClientProt>, PacketHandler<*, out ClientProt>>()

    fun loadHandlersFromPackage(pack: String) {
        try {
            logInfo("Initializing packet handlers ($pack)...")
            val classes = getClasses(pack)

            classes.forEach { clazz ->
                @Suppress("UNCHECKED_CAST")
                mapHandler(clazz.getConstructor().newInstance() as PacketHandler<*, out ClientProt>)
            }
            logInfo("Packet handlers loaded for ${PACKET_HANDLERS.size} packets...")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun mapHandler(handler: PacketHandler<*, out ClientProt>) {
        val type = handler.javaClass.genericInterfaces[0] as ParameterizedType
        @Suppress("UNCHECKED_CAST")
        val clazz = type.actualTypeArguments[1] as Class<ClientProt>
        PACKET_HANDLERS[clazz] = handler
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getHandler(packet: Class<out ClientProt>) = PACKET_HANDLERS[packet] as? PacketHandler<T, ClientProt>
}