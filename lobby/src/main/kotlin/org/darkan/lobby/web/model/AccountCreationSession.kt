package org.darkan.lobby.web.model

import io.ktor.utils.io.*
import org.darkan.core.EnvVars
import org.darkan.core.Logger.logError
import org.darkan.core.isValidEmail
import org.darkan.core.isValidPassword
import org.darkan.core.mongo.collections.Accounts
import org.darkan.core.net.Session
import org.darkan.core.net.prot.*
import world.gregs.voidps.buffer.readRSString
import world.gregs.voidps.cache.secure.Xtea

class AccountCreationSession(val session: Session) {
    suspend fun handleDecodedPackets() {
        for (i in 0 until EnvVars.packetQueueCapacity) {
            val packet = session.readChannel.tryReceive().getOrNull() ?: break
            try {
                when(packet) {
                    is Ping -> session.send(Pong())
                    is AccountCreationStage -> {} //no idea what use this even has lol
                    is CheckEmailValidity -> {
                        Xtea.decipher(packet.encryptedData, session.isaacIn.seed)
                        val read = ByteReadChannel(packet.encryptedData)
                        val proposed = read.readRSString()
                        if (!proposed.isValidEmail()) session.send(CreateCheckEmailReply(21))
                        else if (Accounts.exists(proposed)) session.send(CreateCheckEmailReply(20))
                        else session.send(CreateCheckEmailReply(2))
                    }
                    is SendSignUpForm -> {
                        Xtea.decipher(packet.encryptedData, session.isaacIn.seed)
                        val read = ByteReadChannel(packet.encryptedData)
                        val email = read.readRSString()
                        val password = read.readRSString()
                        if (!email.isValidEmail()) {
                            session.send(CreateAccountReply(CreateAccountReplyOpcode.INVALID_EMAIL))
                            continue
                        }
                        if (!password.isValidPassword()) {
                            session.send(CreateAccountReply(CreateAccountReplyOpcode.PASSWORD_TOO_EASY))
                            println("Invalid password: $password")
                            continue
                        }

                        val account = Accounts.createLobby(email, password)
                        if (account == null) {
                            session.send(CreateAccountReply(CreateAccountReplyOpcode.EMAIL_ALREADY_IN_USE))
                            continue
                        }
                        session.send(CreateAccountReply(CreateAccountReplyOpcode.LOGIN))
                    }
                    else -> println("Unhandled packet: $packet")
                }
            } catch (e: Throwable) {
                logError("Failed to handle packet: $packet", e)
            }
        }
    }
}