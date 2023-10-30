package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import jakarta.inject.Inject
import jakarta.inject.Named

class EntityProcessingContextImpl @Inject constructor(
    @Named("historical") private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    private val state: CottaState
) : EntityProcessingContext {
    override fun fire(effect: CottaEffect) {
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun entities(): List<Entity> {
        TODO();
    }
}