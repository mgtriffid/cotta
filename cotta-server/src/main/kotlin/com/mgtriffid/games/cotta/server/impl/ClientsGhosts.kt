package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.core.entities.PlayerId

class ClientsGhosts {
    val data = HashMap<PlayerId, ClientGhost>()

    // TODO handle removing ghost
    fun addGhost(playerId: PlayerId, connectionId: ConnectionId) {
        data[playerId] = ClientGhost(connectionId)
    }
}
