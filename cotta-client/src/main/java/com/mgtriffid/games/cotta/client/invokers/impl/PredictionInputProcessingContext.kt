package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingInputProcessingContext
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.Traces
import jakarta.inject.Inject
import jakarta.inject.Named

class PredictionInputProcessingContext @Inject constructor(
    @Named("prediction") private val effectBus: EffectBus,
    @Named("prediction") private val traces: Traces
) : TracingInputProcessingContext {
    override fun fire(effect: CottaEffect) {
        effectBus.publisher().fire(effect)
    }

    override fun entities(): List<Entity> {
        TODO("Not yet implemented")
    }

    private var trace: CottaTrace? = null
    override fun setTrace(trace: CottaTrace?) {
        this.trace = trace
    }

    override fun getTrace(): CottaTrace? {
        return trace
    }
}
