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
import kotlinx.coroutines.delay

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
            
            // Always complete login flow first
            completeLogin(read)
        } finally {
            session.exit()
            session.disconnect()
        }
    }
    
    suspend fun completeLogin(read: ByteReadChannel) {
        println("DEBUG: Completing login for user with display name: '${account.displayName}'")
        
        // Try creating a clean account copy
        // If display name is empty, send it as empty to potentially trigger client's built-in name selection
        val displayNameToSend = if (account.displayName.isEmpty()) "" else account.displayName
        val cleanAccount = Account(account.username, account.email, account.passwordHash, displayNameToSend)
        cleanAccount.rights = account.rights
        cleanAccount.banned = 0  // Force to 0
        cleanAccount.muted = 0   // Force to 0
        cleanAccount.prevDisplayName = account.prevDisplayName
        cleanAccount.lastIp = null
        
        println("DEBUG: Sending LobbyLoginDetails packet")
        session.send(LobbyLoginDetails(cleanAccount, worldLoginToken), noIsaac = true)

        println("DEBUG: Account details - username: ${account.username}, email: ${account.email}, displayName: ${account.displayName}")
        
        vars.setVar(281, 1000)
        vars.setVar(2528, 1)
        vars.setVar(2567, 1)
        
        // Set email as validated to avoid email validation interface
        vars.setVarBit(10242, 1) // 1 = email validated
        vars.setVarc(1919, 1) // set email to validated
        
        // Check if display name is empty and set appropriate variables
        if (account.displayName.isEmpty()) {
            println("DEBUG: Display name is empty, triggering display name selection interface")
            
            // Set varBit 10243 to 1 to trigger display name selection
            vars.setVarBit(10243, 1)
            
            // Set display name related variables
            vars.setVar(1384, 1) // Display name selection flag
            vars.setVar(1478, 0) // Display name not set yet
        } else {
            println("DEBUG: Display name exists: ${account.displayName}, entering normal lobby")
            
            // Normal lobby state
            vars.setVarBit(10243, 12)
            vars.setVar(1478, 1) // Display name has been set
        }
        
        vars.setVarBit(11162, 1)
        
        println("DEBUG: Vars set, syncing to client")
        vars.syncVarsToClient()
        
        // Send FriendlistLoaded and UpdateIgnoreList packets to stop loading messages
        session.send(FriendlistLoaded(0))
        session.send(UpdateIgnoreList(0))
        
        // If display name is empty, send instruction for manual command
        if (account.displayName.isEmpty()) {
            delay(200)
            session.send(org.darkan.core.net.prot.GameMessage(
                org.darkan.core.model.ChatMessageType.GAME,
                "Display name required. Type ::setname <name> to set your display name."
            ))
            println("DEBUG: Sent command instruction as fallback")
        }
        
        session.readPackets(read)
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