package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingInputProcessingContext
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.Traces
import jakarta.inject.Inject
import jakarta.inject.Named

class InputProcessingContextImpl @Inject constructor(
    @Named("lagCompensated") private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    private val traces: Traces
) : TracingInputProcessingContext {
    private var trace: CottaTrace? = null
    override fun setTrace(trace: CottaTrace?) {
        this.trace = trace
    }

    override fun getTrace(): CottaTrace? {
        return trace
    }

    override fun fire(effect: CottaEffect) {
        traces.set(effect, trace!!)
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun entities(): List<Entity> {
        TODO("Not yet implemented")
    }
}
