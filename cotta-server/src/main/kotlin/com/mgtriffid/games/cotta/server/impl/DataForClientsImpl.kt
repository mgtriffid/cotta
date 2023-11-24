package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreatedEntities
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.MetaEntities
import jakarta.inject.Inject
import jakarta.inject.Named

data class DataForClientsImpl @Inject constructor(
    val effectsHistory: EffectsHistory,
    val simulationInputHolder: SimulationInputHolder,
    @Named("simulation") val state: CottaState,
    val createdEntities: CreatedEntities,
    val metaEntities: MetaEntities,
    val playersSawTicks: PlayersSawTicks,
) : DataForClients {
    override fun effects(tick: Long): Collection<CottaEffect> {
        return effectsHistory.forTick(tick) // TODO care about tick
    }

    override fun inputs(tick: Long): Map<EntityId, Collection<InputComponent<*>>> {
        return simulationInputHolder.get().inputsForEntities() // TODO care about tick
    }

    override fun entities(tick: Long): Entities {
        return state.entities(tick)
    }

    override fun createdEntities(tick: Long): List<Pair<CottaTrace, EntityId>> {
        return createdEntities.forTick(tick)
    }

    override fun metaEntities(): MetaEntities {
        return metaEntities
    }

    override fun playersSawTicks(): PlayersSawTicks {
        return playersSawTicks
    }
}
