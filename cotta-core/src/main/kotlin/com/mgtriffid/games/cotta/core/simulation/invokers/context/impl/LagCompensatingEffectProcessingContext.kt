package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.ReadingFromPreviousTickEntities
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingEffectProcessingContext
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.Traces
import jakarta.inject.Inject
import jakarta.inject.Named

class LagCompensatingEffectProcessingContext @Inject constructor(
    @Named("lagCompensated") private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    @Named("simulation") private val state: CottaState,
    private val sawTickHolder: SawTickHolder,
    @Named("effectProcessing") private val createEntityStrategy: CreateEntityStrategy,
    private val tickProvider: TickProvider,
    private val traces: Traces
) : TracingEffectProcessingContext {

    private var trace: CottaTrace? = null

    override fun fire(effect: CottaEffect) {
        traces.set(effect, trace!!)
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun entities(): Entities {
        return ReadingFromPreviousTickEntities(
            state = state,
            sawTickHolder = sawTickHolder,
            tickProvider = tickProvider
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
