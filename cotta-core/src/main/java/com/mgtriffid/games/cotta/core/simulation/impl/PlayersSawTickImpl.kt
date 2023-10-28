package com.mgtriffid.games.cotta.core.simulation.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import jakarta.inject.Inject

class PlayersSawTickImpl @Inject constructor(
    private val simulationInputHolder: SimulationInputHolder
): PlayersSawTicks {
    override fun get(playerId: PlayerId): Long? {
        return simulationInputHolder.get().playersSawTicks()[playerId]
    }
}