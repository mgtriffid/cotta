package com.mgtriffid.games.cotta.core.simulation

import com.mgtriffid.games.cotta.core.entities.PlayerId

interface PlayersSawTicks {
    operator fun get(playerId: PlayerId): Long?
    fun all(): Map<PlayerId, Long>
    fun set(playersSawTicks: Map<PlayerId, Long>)
}
