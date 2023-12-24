package com.mgtriffid.games.cotta.core.simulation.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import jakarta.inject.Inject

class PlayersSawTickImpl @Inject constructor(
    private val simulationInputHolder: SimulationInputHolder
) : PlayersSawTicks {
    private var data: Map<PlayerId, Long> = emptyMap()
    override fun get(playerId: PlayerId): Long? {
        return data[playerId]
    }

    override fun all(): Map<PlayerId, Long> {
        return data
//        return simulationInputHolder.get().playersSawTicks().toMap()
    }

    override fun set(playersSawTicks: Map<PlayerId, Long>) {
        data = playersSawTicks
    }
}