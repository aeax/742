package org.darkan.lobby.server.packet

import org.darkan.core.net.prot.IfButton
import org.darkan.core.net.prot.handler.PacketHandler
import org.darkan.lobby.web.model.LobbyPlayer

class IfButtonHandler : PacketHandler<LobbyPlayer, IfButton> {
    override suspend fun handle(player: LobbyPlayer, packet: IfButton) {
        println("DEBUG: IfButton clicked - interfaceId: ${packet.interfaceId}, componentId: ${packet.componentId}, opNum: ${packet.opNum}")
        
        // World list interface appears to be 906, check if this is a world click
        if (packet.interfaceId == 906) {
            println("DEBUG: World list interface clicked - componentId: ${packet.componentId}")
            
            // World clicks typically have specific component IDs, let's log them all to identify the pattern
            // For now, let's assume any click on the world list interface with opNum 0 is a world selection
            if (packet.opNum == 0 && packet.componentId != 215 && packet.componentId != 509) {
                println("DEBUG: Possible world selection - componentId: ${packet.componentId}")
                
                // TODO: Determine which world was clicked and initiate world login
                // For now, let's see what component IDs correspond to world clicks
            }
        }
    }
}