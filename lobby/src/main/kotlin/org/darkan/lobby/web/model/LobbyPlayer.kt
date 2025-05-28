package org.darkan.lobby.web.model

import io.ktor.utils.io.*
import org.darkan.core.EnvVars
import org.darkan.core.Logger.logDebug
import org.darkan.core.Logger.logError
import org.darkan.core.generateRandomString
import org.darkan.core.net.Session
import org.darkan.core.net.prot.LobbyLoginDetails
import org.darkan.core.net.prot.Ping
import org.darkan.core.net.prot.WorldListPacket
import org.darkan.core.net.prot.handler.PacketHandlers
import org.darkan.core.model.Account
import org.darkan.core.model.Vars
import org.darkan.lobby.Lobby

class LobbyPlayer(val session: Session, val account: Account) {
    val vars = Vars().setSession(session)
    val worldLoginToken = generateRandomString()

    suspend fun login(read: ByteReadChannel) {
        try {
            session.send(LobbyLoginDetails(account, worldLoginToken), noIsaac = true)
            session.send(WorldListPacket(Lobby.worldList, true, false))
            vars.setVar(281, 1000)
            vars.setVar(2528, 1)
            vars.setVar(2567, 1)

            vars.setVarBit(10242, 1) // 2 for validating email address
            vars.setVarBit(10243, 12)

            vars.setVarc(1919, 1) // set email to validated
            vars.setVarBit(11162, 1)
            vars.syncVarsToClient()
            session.readPackets(read)
        } finally {
            session.exit()
            session.disconnect()
        }
    }

    suspend fun handleDecodedPackets() {
        for (i in 0 until EnvVars.packetQueueCapacity) {
            val packet = session.readChannel.tryReceive().getOrNull() ?: break
            if (packet !is Ping)
                logDebug("Handling packet: $packet")
            try {
                PacketHandlers.getHandler<LobbyPlayer>(packet.javaClass)?.handle(this, packet)
            } catch (e: Throwable) {
                logError("Failed to handle packet: $packet", e)
            }
        }
    }
}