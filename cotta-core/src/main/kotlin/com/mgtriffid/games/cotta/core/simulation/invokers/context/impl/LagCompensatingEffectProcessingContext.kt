package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.invokers.ReadingFromPreviousTickEntities
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import jakarta.inject.Inject
import jakarta.inject.Named

class LagCompensatingEffectProcessingContext @Inject constructor(
    private val effectBus: EffectBus,
    @Named("simulation") private val state: CottaState,
    private val sawTickHolder: SawTickHolder,
    @Named("effectProcessing") private val createEntityStrategy: CreateEntityStrategy,
    @Named(SIMULATION) private val tickProvider: TickProvider,
    private val clock: CottaClock,
) : EffectProcessingContext {

    override fun fire(effect: CottaEffect) {
        effectBus.publisher().fire(effect)
    }

    override fun clock(): CottaClock {
        return clock
    }

    override fun entities(): Entities {
        return ReadingFromPreviousTickEntities(
            state = state,
            sawTickHolder = sawTickHolder,
            tickProvider = tickProvider
        )
    }

    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return createEntityStrategy.createEntity(ownedBy)
    }
}
