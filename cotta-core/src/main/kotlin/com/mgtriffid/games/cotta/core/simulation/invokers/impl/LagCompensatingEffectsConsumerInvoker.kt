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

// TODO forbid creation of Entities here or make Entities created in the latest tick
class LagCompensatingEffectsConsumerInvoker @Inject constructor(
    @Named("historical") private val effectBus: LagCompensatingEffectBus,
    private val sawTickHolder: SawTickHolder,
    @Named("lagCompensated") private val context: TracingEffectProcessingContext,
    private val traces: Traces
) : SystemInvoker<EffectsConsumerSystem> {
    override fun invoke(system: EffectsConsumerSystem) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        if (system::class.simpleName == "MovementEffectConsumerSystem") {
            logger.info { "Invoked MovementEffectConsumerSystem in simulation" }
        }
        effectBus.effects().forEach { process(it, system) }
        if (system::class.simpleName == "MovementEffectConsumerSystem") {
            logger.info { "Done invoking MovementEffectConsumerSystem in simulation" }
        }
    }

    private fun process(effect: CottaEffect, system: EffectsConsumerSystem) {
        logger.debug { "${system::class.simpleName} processing effect $effect" }
        sawTickHolder.tick = effectBus.getTickForEffect(effect)
        context.setTrace(
            traces.get(effect)?.plus(TraceElement.EffectTraceElement(effect))
                ?: CottaTrace.from(TraceElement.EffectTraceElement(effect))
        )
        system.handle(effect, context)
        context.setTrace(null)
        sawTickHolder.tick = null
    }
}
