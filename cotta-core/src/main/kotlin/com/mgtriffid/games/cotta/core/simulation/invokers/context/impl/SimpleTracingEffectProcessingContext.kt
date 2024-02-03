package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.LatestEntities
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingEffectProcessingContext
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.Traces
import jakarta.inject.Inject
import jakarta.inject.Named

class SimpleTracingEffectProcessingContext @Inject constructor(
    @Named("lagCompensated") private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    @Named("simulation") private val state: CottaState,
    private val sawTickHolder: SawTickHolder,
    @Named("effectProcessing") private val createEntityStrategy: CreateEntityStrategy,
    private val tickProvider: TickProvider,
    private val clock: CottaClock,
    private val traces: Traces
)  : TracingEffectProcessingContext {
    private var trace: CottaTrace? = null

    override fun fire(effect: CottaEffect) {
        traces.set(effect, trace!!)
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun clock(): CottaClock {
        return clock
    }

    override fun entities(): Entities {
        return LatestEntities(
            state = state,
            tick = tickProvider
        )
    }

    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return createEntityStrategy.createEntity(ownedBy, trace ?: throw IllegalStateException("Expected trace to be set"))
    }

    override fun setTrace(trace: CottaTrace?) {
        this.trace = trace
    }

    override fun getTrace(): CottaTrace? {
        return trace
    }
}