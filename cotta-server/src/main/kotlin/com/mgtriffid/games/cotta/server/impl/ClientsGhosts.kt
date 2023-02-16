package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.server.PlayerId

class ClientsGhosts {
    val data = HashMap<PlayerId, ClientGhost>()

    fun addGhost(playerId: PlayerId, connectionId: ConnectionId) {
        data[playerId] = ClientGhost(connectionId)
    }
}
