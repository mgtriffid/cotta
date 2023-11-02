package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import jakarta.inject.Inject
import jakarta.inject.Named

class InputProcessingContextImpl @Inject constructor(
    @Named("lagCompensated") private val lagCompensatingEffectBus: LagCompensatingEffectBus
) : InputProcessingContext{
    override fun fire(effect: CottaEffect) {
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun entities(): List<Entity> {
        TODO("Not yet implemented")
    }
}