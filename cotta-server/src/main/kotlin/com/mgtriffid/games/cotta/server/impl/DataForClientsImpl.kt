package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreatedEntities
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.MetaEntities
import com.mgtriffid.games.cotta.server.PredictedToAuthoritativeIdMappings
import jakarta.inject.Inject
import jakarta.inject.Named

data class DataForClientsImpl @Inject constructor(
    private val effectsHistory: EffectsHistory,
    private val simulationInputHolder: SimulationInputHolder,
    @Named("simulation") private val state: CottaState,
    private val createdEntities: CreatedEntities,
    private val metaEntities: MetaEntities,
    private val playersSawTicks: PlayersSawTicks,
    private val predictedToAuthoritativeIdMappings: PredictedToAuthoritativeIdMappings,
) : DataForClients {
    override fun effects(tick: Long): Collection<CottaEffect> {
        return effectsHistory.forTick(tick)
    }

    override fun inputs(): Map<EntityId, Collection<InputComponent<*>>> {
        return simulationInputHolder.get().inputsForEntities()
    }

    override fun playerInputs(): Map<PlayerId, PlayerInput> {
        return simulationInputHolder.get().inputForPlayers()
    }

    override fun entities(tick: Long): Entities {
        return state.entities(tick)
    }

    override fun createdEntities(tick: Long): List<Pair<CottaTrace, EntityId>> {
        return createdEntities.forTick(tick)
    }

    override fun confirmedEntities(tick: Long): List<Pair<PredictedEntityId, AuthoritativeEntityId>> {
        return predictedToAuthoritativeIdMappings.forTick(tick)
    }

    override fun metaEntities(): MetaEntities {
        return metaEntities
    }

    override fun playersSawTicks(): PlayersSawTicks {
        return playersSawTicks
    }
}
