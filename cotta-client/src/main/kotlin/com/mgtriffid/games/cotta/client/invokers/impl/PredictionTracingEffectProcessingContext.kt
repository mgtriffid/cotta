package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import jakarta.inject.Inject
import jakarta.inject.Named

class PredictionTracingEffectProcessingContext @Inject constructor(
    @Named("prediction") private val createEntityStrategy: CreateEntityStrategy,
    @Named("prediction") private val entities: Entities,
    @Named("prediction") private val clock: CottaClock,
    @Named("prediction") private val effectBus: EffectBus,
) : EffectProcessingContext {

    override fun fire(effect: CottaEffect) {
        effectBus.publisher().fire(effect)
    }

    override fun entities(): Entities {
        return entities // not sure
    }

    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return createEntityStrategy.createEntity(ownedBy)
    }

    override fun clock(): CottaClock {
        return clock
    }
}
