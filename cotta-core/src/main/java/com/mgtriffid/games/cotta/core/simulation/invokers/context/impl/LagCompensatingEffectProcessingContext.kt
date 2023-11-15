package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.ReadingFromPreviousTickEntities
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import jakarta.inject.Inject
import jakarta.inject.Named

class LagCompensatingEffectProcessingContext @Inject constructor(
    @Named("lagCompensated") private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    @Named("simulation") private val state: CottaState,
    private val sawTickHolder: SawTickHolder,
    @Named("effectProcessing") private val createEntityStrategy: CreateEntityStrategy
) : EffectProcessingContext {
    override fun fire(effect: CottaEffect) {
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun entities(): Entities {
        return ReadingFromPreviousTickEntities(state = state, sawTickHolder = sawTickHolder)
    }

    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return createEntityStrategy.createEntity(ownedBy)
    }
}
