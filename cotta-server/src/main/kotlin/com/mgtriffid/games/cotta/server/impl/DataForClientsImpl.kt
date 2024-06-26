package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesInternal
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.core.simulation.Players
import jakarta.inject.Inject
import jakarta.inject.Named

data class DataForClientsImpl @Inject constructor(
    private val simulationInputHolder: SimulationInputHolder,
    @Named("simulation") private val state: CottaState,
    private val players: Players,
    private val playersSawTicks: PlayersSawTicks,
) : DataForClients {
    override fun playerInputs(): Map<PlayerId, PlayerInput> {
        return simulationInputHolder.get().inputForPlayers()
    }

    override fun entities(tick: Long): EntitiesInternal {
        return state.entities(tick)
    }

    override fun idSequence(tick: Long): Int {
        return state.entities(tick).currentId()
    }

    override fun players(): Players {
        return players
    }

    override fun playersSawTicks(): PlayersSawTicks {
        return playersSawTicks
    }
}
