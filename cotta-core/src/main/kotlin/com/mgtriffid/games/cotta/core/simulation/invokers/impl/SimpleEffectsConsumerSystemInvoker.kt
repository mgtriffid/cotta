package com.mgtriffid.games.cotta.core.simulation.invokers.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SimpleEffectsConsumerSystemInvoker @Inject constructor(
    @Named("lagCompensated") private val effectBus: LagCompensatingEffectBus,
    @Named("simple") private val context: EffectProcessingContext,
    private val sawTickHolder: SawTickHolder,
) : SystemInvoker<EffectsConsumerSystem<*>> {
    override fun invoke(system: EffectsConsumerSystem<*>) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        effectBus.effects().forEach { process(it, system) }
    }

    private fun <T: CottaEffect> process(effect: CottaEffect, system: EffectsConsumerSystem<T>) {
        logger.debug { "${system::class.simpleName} processing effect $effect" }
        sawTickHolder.tick = effectBus.getTickForEffect(effect)
        if (system.effectType.isAssignableFrom(effect.javaClass)) {
            system.handle(system.effectType.cast(effect), context)
        }
        sawTickHolder.tick = null
    }
}
