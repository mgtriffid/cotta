package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import jakarta.inject.Inject
import jakarta.inject.Named

class PredictionInputProcessingContext @Inject constructor(
    @Named("prediction") effectBus: EffectBus
) : InputProcessingContext {
    override fun fire(effect: CottaEffect) {
//        TODO("Not yet implemented")
    }

    override fun entities(): List<Entity> {
        TODO("Not yet implemented")
    }
}
