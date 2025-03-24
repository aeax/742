package org.darkan.lobby.web.model

import io.ktor.utils.io.*
import kotlinx.coroutines.channels.Channel
import org.darkan.core.Logger.logSevere
import org.darkan.core.net.Session
import org.darkan.core.net.prot.ClientProt
import org.darkan.core.net.prot.LobbyLoginDetails
import org.darkan.core.net.prot.ProtSize
import org.darkan.core.type.Account
import world.gregs.voidps.buffer.readUByte
import world.gregs.voidps.buffer.readUShort

class LobbyPlayer(val session: Session, val account: Account) {
    val readChannel = Channel<ClientProt>(capacity = 50)

    suspend fun login(read: ByteReadChannel) {
        try {
            session.send(LobbyLoginDetails(account, "982938jf"), noIsaac = true)
            session.flush()
            readPackets(read)
        } finally {
            session.exit()
            session.disconnect()
        }
    }

    suspend fun readPackets(read: ByteReadChannel) {
        while (!session.disconnected) {
            val cipher = session.isaacIn.nextInt()
            val opcode = (read.readUByte() - cipher) and 0xff
            val clientProt = session.codec.clientProtsByOpcode[opcode]
            if (clientProt == null) {
                logSevere("Missing ClientProt with opcode $opcode")
                return
            }
            val size = when (clientProt.size) {
                is ProtSize.Fixed -> (clientProt.size as ProtSize.Fixed).length
                ProtSize.VarByte -> read.readUByte()
                ProtSize.VarShort -> read.readUShort()
            }
            val packet = read.readPacket(size)
            readChannel.send(clientProt.decoder?.invoke(packet, size) ?: continue)
        }
    }
}