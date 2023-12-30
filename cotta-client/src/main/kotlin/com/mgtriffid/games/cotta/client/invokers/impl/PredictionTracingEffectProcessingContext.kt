package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingEffectProcessingContext
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.Traces
import jakarta.inject.Inject
import jakarta.inject.Named

class PredictionTracingEffectProcessingContext @Inject constructor(
    @Named("prediction") private val traces: Traces,
    @Named("prediction") private val createEntityStrategy: CreateEntityStrategy,
    @Named("prediction") private val entities: Entities,
    @Named("prediction") private val clock: CottaClock,
) : TracingEffectProcessingContext {

    override fun fire(effect: CottaEffect) {
        TODO("Not yet implemented")
    }

    override fun entities(): Entities {
        return entities // not sure
    }

    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return createEntityStrategy.createEntity(ownedBy, trace ?: throw IllegalStateException("Expected trace to be set"))
    }

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
}