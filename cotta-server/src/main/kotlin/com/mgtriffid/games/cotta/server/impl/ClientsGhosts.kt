package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.server.PlayerId

class ClientsGhosts {
    val data = HashMap<PlayerId, ClientGhost>()

    fun addGhost(playerId: PlayerId) {
        data[playerId] = ClientGhost()
    }
}
