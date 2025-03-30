package org.darkan.lobby.server.packet

import org.darkan.core.net.prot.RequestWorldList
import org.darkan.core.net.prot.WorldListPacket
import org.darkan.core.net.prot.handler.PacketHandler
import org.darkan.lobby.Lobby
import org.darkan.lobby.web.model.LobbyPlayer

class RequestWorldListHandler : PacketHandler<LobbyPlayer, RequestWorldList> {
    override suspend fun handle(player: LobbyPlayer, packet: RequestWorldList) {
        player.session.send(WorldListPacket(Lobby.worldList, packet.worldlistVersion != Lobby.worldList.revision, false))
        player.session.flush()
    }
}