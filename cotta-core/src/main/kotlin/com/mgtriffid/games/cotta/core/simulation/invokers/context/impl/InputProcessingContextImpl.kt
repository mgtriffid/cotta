package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingInputProcessingContext
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.Traces
import jakarta.inject.Inject
import jakarta.inject.Named

class InputProcessingContextImpl @Inject constructor(
    @Named("lagCompensated") private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    private val clock: CottaClock,
    private val traces: Traces,
    @Named("latest") private val entities: Entities
) : TracingInputProcessingContext {
    private var trace: CottaTrace? = null

    override fun setTrace(trace: CottaTrace?) {
        this.trace = trace
    }

    override fun getTrace(): CottaTrace? {
        return trace
    }

    override fun clock(): CottaClock {
        return clock
    }

    override fun fire(effect: CottaEffect) {
        traces.set(effect, trace!!)
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun entities(): Entities {
        return entities
    }
}
