package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.network.ConnectionId

class ClientsGhosts {

    val playerByConnection = HashMap<ConnectionId, PlayerId>()
    val data = HashMap<PlayerId, ClientGhost>()

    // TODO handle removing ghost
    fun addGhost(playerId: PlayerId, connectionId: ConnectionId) {
        data[playerId] = ClientGhost(connectionId)
        playerByConnection[connectionId] = playerId
    }
}
