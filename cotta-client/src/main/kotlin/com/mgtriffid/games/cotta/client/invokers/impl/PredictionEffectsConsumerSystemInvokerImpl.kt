package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.client.invokers.PredictionEffectsConsumerSystemInvoker
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingEffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.Traces
import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PredictionEffectsConsumerSystemInvokerImpl @Inject constructor(
    @Named("prediction") private val effectBus: EffectBus,
    @Named("prediction") private val context: TracingEffectProcessingContext,
    @Named("prediction") private val traces: Traces,
) : PredictionEffectsConsumerSystemInvoker {
    override fun invoke(system: EffectsConsumerSystem<*>) {
        effectBus.effects().forEach { process(it, system) }
    }

    private fun <T: CottaEffect> process(effect: CottaEffect, system: EffectsConsumerSystem<T>) {
        logger.debug { "${system::class.simpleName} processing effect $effect" }
        context.setTrace(
            traces.get(effect)?.plus(TraceElement.EffectTraceElement(effect))
                ?: CottaTrace.from(TraceElement.EffectTraceElement(effect))
        )
        if (system.effectType.isAssignableFrom(effect::class.java)) {
            system.handle(system.effectType.cast(effect), context)
        }
        context.setTrace(null)
    }
}
