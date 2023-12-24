package com.mgtriffid.games.cotta.core.simulation.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import jakarta.inject.Inject

class PlayersSawTickImpl : PlayersSawTicks {
    private var data: Map<PlayerId, Long> = emptyMap()

    override fun get(playerId: PlayerId): Long? {
        return data[playerId]
    }

    override fun all(): Map<PlayerId, Long> {
        return data
    }

    override fun set(playersSawTicks: Map<PlayerId, Long>) {
        data = playersSawTicks
    }
}