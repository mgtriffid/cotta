package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientPlayers
import com.mgtriffid.games.cotta.core.entities.PlayerId

class ClientPlayersImpl : ClientPlayers {
    private val players = mutableListOf<PlayerId>()

    override fun add(playerId: PlayerId) {
        players.add(playerId)
    }

    override fun all(): List<PlayerId> {
        return players
    }
}
