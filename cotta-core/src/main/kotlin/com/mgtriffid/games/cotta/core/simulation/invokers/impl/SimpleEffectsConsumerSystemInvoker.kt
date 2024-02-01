package com.mgtriffid.games.cotta.core.simulation.invokers.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingEffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.Traces
import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SimpleEffectsConsumerSystemInvoker @Inject constructor(
    @Named("lagCompensated") private val effectBus: LagCompensatingEffectBus,
    @Named("simple") private val context: TracingEffectProcessingContext,
    private val sawTickHolder: SawTickHolder,
    private val traces: Traces
) : SystemInvoker<EffectsConsumerSystem> {
    override fun invoke(system: EffectsConsumerSystem) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        effectBus.effects().forEach { process(it, system) }
    }

    private fun process(effect: CottaEffect, system: EffectsConsumerSystem) {
        logger.debug { "${system::class.simpleName} processing effect $effect" }
        context.setTrace(
            traces.get(effect)?.plus(TraceElement.EffectTraceElement(effect))
                ?: CottaTrace.from(TraceElement.EffectTraceElement(effect))
        )
        sawTickHolder.tick = effectBus.getTickForEffect(effect)
        system.handle(effect, context)
        sawTickHolder.tick = null
        context.setTrace(null)
    }
}
