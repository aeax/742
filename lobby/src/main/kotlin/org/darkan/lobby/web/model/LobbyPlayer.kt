package org.darkan.lobby.web.model

import io.ktor.utils.io.*
import org.darkan.core.EnvVars
import org.darkan.core.Logger.logDebug
import org.darkan.core.Logger.logError
import org.darkan.core.generateRandomString
import org.darkan.core.net.Session
import org.darkan.core.net.prot.IfButton
import org.darkan.core.net.prot.LobbyLoginDetails
import org.darkan.core.net.prot.Ping
import org.darkan.core.net.prot.WorldListPacket
import org.darkan.core.net.prot.FriendlistLoaded
import org.darkan.core.net.prot.UpdateIgnoreList
import org.darkan.core.net.prot.handler.PacketHandlers
import org.darkan.core.model.Account
import org.darkan.core.model.Vars
import org.darkan.lobby.Lobby

class LobbyPlayer(val session: Session, val account: Account) {
    val vars = Vars().setSession(session)
    // Try using a fixed token to see if changing tokens triggers the password update message
    val worldLoginToken = "FIXED_TOKEN_TEST" // generateRandomString()

    suspend fun login(read: ByteReadChannel) {
        try {
            println("DEBUG: Sending LobbyLoginDetails with token: $worldLoginToken")
            println("DEBUG: Full Account details:")
            println("  - username: '${account.username}'")
            println("  - email: '${account.email}'")
            println("  - displayName: '${account.displayName}'")
            println("  - prevDisplayName: '${account.prevDisplayName}'")
            println("  - passwordHash: '${account.passwordHash}'")
            println("  - legacyPass: ${account.legacyPass}")
            println("  - password: ${account.password?.contentToString()}")
            println("  - rights: ${account.rights}")
            println("  - banned: ${account.banned}")
            println("  - muted: ${account.muted}")
            println("  - lastIp: ${account.lastIp}")
            println("  - previousPasswords size: ${account.previousPasswords.size}")
            println("  - social.friends size: ${account.social.friends.size}")
            
            // Try creating a clean account copy to see if some field is causing issues
            // For first-time login, displayName should be null/empty to trigger "New character name" prompt
            val cleanAccount = Account(account.username, account.email, account.passwordHash, "")
            cleanAccount.rights = account.rights
            cleanAccount.banned = 0  // Force to 0
            cleanAccount.muted = 0   // Force to 0
            cleanAccount.prevDisplayName = ""
            cleanAccount.lastIp = null
            
            println("DEBUG: Skipping LobbyLoginDetails packet to test")
             session.send(LobbyLoginDetails(cleanAccount, worldLoginToken), noIsaac = true)

            println("DEBUG: Account details - username: ${account.username}, email: ${account.email}, displayName: ${account.displayName}")
            
            vars.setVar(281, 1000)
            vars.setVar(2528, 1)
            vars.setVar(2567, 1)
            
            // These seem required for lobby to work
            vars.setVarBit(10242, 1) // 2 for validating email address
            vars.setVarBit(10243, 12)
            
            // Skip the email validated var to see if this is the trigger
             vars.setVarc(1919, 1) // set email to validated
            
            vars.setVarBit(11162, 1)
            
            println("DEBUG: Vars set, syncing to client")
            vars.syncVarsToClient()
            
            // Send FriendlistLoaded and UpdateIgnoreList packets to stop loading messages
            session.send(FriendlistLoaded(0))
            session.send(UpdateIgnoreList(0))
            
            session.readPackets(read)
        } finally {
            session.exit()
            session.disconnect()
        }
    }

    suspend fun handleDecodedPackets() {
        for (i in 0 until EnvVars.packetQueueCapacity) {
            val packet = session.readChannel.tryReceive().getOrNull() ?: break
            if (packet !is Ping) {
                logDebug("Handling packet: $packet")
                // Log all packets to console to help debug world clicks
                println("DEBUG: Received packet: $packet")
            }
            try {
                PacketHandlers.getHandler<LobbyPlayer>(packet.javaClass)?.handle(this, packet)
            } catch (e: Throwable) {
                logError("Failed to handle packet: $packet", e)
            }
        }
    }
}