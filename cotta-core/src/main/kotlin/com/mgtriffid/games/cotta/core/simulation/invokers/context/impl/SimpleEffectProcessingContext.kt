package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.LatestEntities
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import jakarta.inject.Inject
import jakarta.inject.Named

class SimpleEffectProcessingContext @Inject constructor(
    @Named("lagCompensated") private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    @Named("simulation") private val state: CottaState,
    @Named("effectProcessing") private val createEntityStrategy: CreateEntityStrategy,
    @Named(SIMULATION) private val tickProvider: TickProvider,
    private val clock: CottaClock,
)  : EffectProcessingContext {

    override fun fire(effect: CottaEffect) {
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun clock(): CottaClock {
        return clock
    }

    override fun entities(): Entities {
        return LatestEntities(
            state = state,
            tick = tickProvider
        )
    }

    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return createEntityStrategy.createEntity(ownedBy)
    }
}
