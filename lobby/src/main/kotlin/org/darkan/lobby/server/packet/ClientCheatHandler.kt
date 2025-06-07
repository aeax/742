package org.darkan.lobby.server.packet

import org.darkan.core.net.prot.ClientCheat
import org.darkan.core.net.prot.GameMessage
import org.darkan.core.model.ChatMessageType
import org.darkan.core.mongo.collections.Accounts
import org.darkan.lobby.web.model.LobbyPlayer
import org.darkan.core.net.prot.handler.PacketHandler

class ClientCheatHandler : PacketHandler<LobbyPlayer, ClientCheat> {
    
    override suspend fun handle(player: LobbyPlayer, packet: ClientCheat) {
        val command = packet.command.trim()
        println("DEBUG: Received command: '${command}'")
        
        if (command.startsWith("setname ")) {
            handleSetNameCommand(player, command.substring(8).trim())
        } else {
            player.session.send(GameMessage(ChatMessageType.GAME, "Unknown command: $command"))
        }
    }
    
    private suspend fun handleSetNameCommand(player: LobbyPlayer, name: String) {
        if (name.isEmpty()) {
            player.session.send(GameMessage(ChatMessageType.GAME, "Usage: ::setname <display name>"))
            return
        }
        
        // Validate display name
        val validationResult = validateDisplayName(name)
        if (!validationResult.isValid) {
            player.session.send(GameMessage(ChatMessageType.GAME, validationResult.reason))
            return
        }
        
        // Check if name is already taken
        if (isDisplayNameTaken(name)) {
            player.session.send(GameMessage(ChatMessageType.GAME, "Display name '$name' is already taken."))
            return
        }
        
        // Save the display name
        val account = player.account
        account.displayName = name
        Accounts.save(account)
        
        player.session.send(GameMessage(ChatMessageType.GAME, "Display name set to: $name"))
        println("DEBUG: Display name set via command to: '$name' for account: ${account.username}")
    }
    
    private data class ValidationResult(val isValid: Boolean, val reason: String = "")
    
    private fun validateDisplayName(name: String): ValidationResult {
        // Check length
        if (name.length < 1) {
            return ValidationResult(false, "Display name cannot be empty.")
        }
        if (name.length > 12) {
            return ValidationResult(false, "Display name cannot be longer than 12 characters.")
        }
        
        // Check for only whitespace
        if (name.isBlank()) {
            return ValidationResult(false, "Display name cannot be only spaces.")
        }
        
        // Check for invalid characters (allow letters, numbers, spaces, hyphens, underscores)
        val validPattern = Regex("^[a-zA-Z0-9 _-]+$")
        if (!validPattern.matches(name)) {
            return ValidationResult(false, "Display name can only contain letters, numbers, spaces, hyphens and underscores.")
        }
        
        // Check for consecutive spaces
        if (name.contains("  ")) {
            return ValidationResult(false, "Display name cannot contain consecutive spaces.")
        }
        
        // Check for leading/trailing spaces
        if (name != name.trim()) {
            return ValidationResult(false, "Display name cannot start or end with spaces.")
        }
        
        // Check for inappropriate words (basic filter)
        val bannedWords = listOf(
            "admin", "mod", "moderator", "jagex", "staff", "owner", 
            "null", "undefined", "test", "debug", "system"
        )
        val lowerName = name.lowercase()
        for (bannedWord in bannedWords) {
            if (lowerName.contains(bannedWord)) {
                return ValidationResult(false, "Display name contains inappropriate content.")
            }
        }
        
        return ValidationResult(true)
    }
    
    private suspend fun isDisplayNameTaken(name: String): Boolean {
        return Accounts.findByDisplayName(name) != null
    }
}