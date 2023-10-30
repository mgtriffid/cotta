package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LagCompensatingEffectsConsumerInvoker @Inject constructor(
    @Named("lagCompensated") private val effectBus: LagCompensatingEffectBus,
    private val sawTickHolder: SawTickHolder,
    @Named("lagCompensated") private val context: EffectProcessingContext
) : SystemInvoker<EffectsConsumerSystem> {
    override fun invoke(system: EffectsConsumerSystem) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        effectBus.effects().forEach { process(it, system) }
    }

    private fun process(effect: CottaEffect, system: EffectsConsumerSystem) {
        logger.debug { "${system::class.simpleName} processing effect $effect" }
        sawTickHolder.tick = effectBus.getTickForEffect(effect)
        system.handle(effect, context)
        sawTickHolder.tick = null
    }
}
