package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import jakarta.inject.Inject
import jakarta.inject.Named

class EntityProcessingContextImpl @Inject constructor(
    @Named("historical") private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    @Named("latest") private val entities: Entities,
    private val clock: CottaClock,
) : EntityProcessingContext {
    override fun fire(effect: CottaEffect) {
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun entities(): Entities {
        return entities
    }

    override fun clock(): CottaClock {
        return clock
    }
}
