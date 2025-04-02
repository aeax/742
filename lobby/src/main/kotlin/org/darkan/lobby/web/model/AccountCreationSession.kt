package org.darkan.lobby.web.model

import org.darkan.core.EnvVars
import org.darkan.core.Logger.logDebug
import org.darkan.core.Logger.logError
import org.darkan.core.isValidEmail
import org.darkan.core.mongo.collections.Accounts
import org.darkan.core.net.Session
import org.darkan.core.net.prot.CheckEmailValidity
import org.darkan.core.net.prot.CreateCheckEmailReply
import org.darkan.core.net.prot.Ping
import org.darkan.core.net.prot.Pong
import world.gregs.voidps.cache.secure.Xtea

class AccountCreationSession(val session: Session) {


    suspend fun handleDecodedPackets() {
        for (i in 0 until EnvVars.packetQueueCapacity) {
            val packet = session.readChannel.tryReceive().getOrNull() ?: break
            if (packet !is Ping)
                logDebug("Handling packet: $packet")
            try {
                when(packet) {
                    is Ping -> session.send(Pong())
                    is CheckEmailValidity -> {
                        Xtea.decipher(packet.encryptedData, session.isaacIn.seed)
                        val email = packet.encryptedData.toString(Charsets.ISO_8859_1)
                        println("Checking email: $email")
                        if (!email.isValidEmail()) session.send(CreateCheckEmailReply(21))
                        else if (Accounts.exists(email)) session.send(CreateCheckEmailReply(20))
                        else session.send(CreateCheckEmailReply(2))
                    }
                    else -> println("Unhandled packet: $packet")
                }
            } catch (e: Throwable) {
                logError("Failed to handle packet: $packet", e)
            }
        }
    }
}