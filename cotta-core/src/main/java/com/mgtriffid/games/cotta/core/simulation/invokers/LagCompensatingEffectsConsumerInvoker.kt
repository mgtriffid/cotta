package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LagCompensatingEffectsConsumerInvoker(
    private val effectBus: LagCompensatingEffectBus,
    private val consumer: EffectsConsumer,
    private val sawTickHolder: InvokersFactoryImpl.SawTickHolder
) : SystemInvoker {
    override fun invoke() {
        logger.debug { "Invoked ${consumer::class.qualifiedName}" }
        effectBus.effects().forEach(::process)
    }

    private fun process(effect: CottaEffect) {
        logger.debug { "${consumer::class.simpleName} processing effect $effect" }
        sawTickHolder.tick = effectBus.getTickForEffect(effect)
        consumer.handleEffect(effect)
        sawTickHolder.tick = null
    }
}
