package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.PlayerId

interface ClientPlayers {
    fun add(playerId: PlayerId)
    fun all(): List<PlayerId>
}
