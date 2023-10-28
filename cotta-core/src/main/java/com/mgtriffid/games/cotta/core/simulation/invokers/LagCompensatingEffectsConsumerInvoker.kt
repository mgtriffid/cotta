package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LagCompensatingEffectsConsumerInvoker(
    private val effectBus: LagCompensatingEffectBus,
    private val sawTickHolder: InvokersFactoryImpl.SawTickHolder
) : SystemInvoker<EffectsConsumerSystem> {
    override fun invoke(system: EffectsConsumerSystem) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        effectBus.effects().forEach { process(it, system) }
    }

    private fun process(effect: CottaEffect, system: EffectsConsumerSystem) {
        logger.debug { "${system::class.simpleName} processing effect $effect" }
        sawTickHolder.tick = effectBus.getTickForEffect(effect)
        system.handle(effect)
        sawTickHolder.tick = null
    }
}
