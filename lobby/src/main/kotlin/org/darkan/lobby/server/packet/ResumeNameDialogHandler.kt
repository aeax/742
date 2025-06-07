package org.darkan.lobby.server.packet

import org.darkan.core.net.prot.ResumeNameDialog
import org.darkan.core.net.prot.GameMessage
import org.darkan.core.model.ChatMessageType
import org.darkan.core.mongo.collections.Accounts
import org.darkan.lobby.web.model.LobbyPlayer
import org.darkan.core.net.prot.handler.PacketHandler

class ResumeNameDialogHandler : PacketHandler<LobbyPlayer, ResumeNameDialog> {
    
    override suspend fun handle(player: LobbyPlayer, packet: ResumeNameDialog) {
        val account = player.account
        
        val chosenName = packet.name.trim()
        
        println("DEBUG: Player ${account.username} chose display name: '$chosenName'")
        
        // Validate display name
        val validationResult = validateDisplayName(chosenName)
        if (!validationResult.isValid) {
            println("DEBUG: Display name validation failed: ${validationResult.reason}")
            player.session.send(GameMessage(ChatMessageType.GAME, validationResult.reason))
            // Set variables to reopen the dialog
            player.vars.setVarBit(10243, 1)
            player.vars.setVar(1384, 1)
            player.vars.setVar(1478, 0)
            player.vars.syncVarsToClient()
            return
        }
        
        // Check if name is already taken
        if (isDisplayNameTaken(chosenName)) {
            println("DEBUG: Display name '$chosenName' is already taken")
            player.session.send(GameMessage(ChatMessageType.GAME, "Display name is already taken. Please choose another."))
            // Set variables to reopen the dialog
            player.vars.setVarBit(10243, 1)
            player.vars.setVar(1384, 1)
            player.vars.setVar(1478, 0)
            player.vars.syncVarsToClient()
            return
        }
        
        // Save the display name
        account.displayName = chosenName
        Accounts.save(account)
        
        println("DEBUG: Display name set to: '$chosenName' for account: ${account.username}")
        
        // Send confirmation message
        player.session.send(GameMessage(ChatMessageType.GAME, "Display name set to: $chosenName"))
        
        // Reset the variables to normal state
        player.vars.setVarBit(10243, 12) // Set back to normal lobby state
        player.vars.setVar(1384, 0) // Clear display name selection flag
        player.vars.setVar(1478, 1) // Mark display name as set
        player.vars.syncVarsToClient()
        
        println("DEBUG: Display name set successfully, login should continue normally")
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