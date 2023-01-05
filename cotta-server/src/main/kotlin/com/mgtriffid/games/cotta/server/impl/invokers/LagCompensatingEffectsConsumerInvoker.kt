package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer

class LagCompensatingEffectsConsumerInvoker(
    private val effectBus: LagCompensatingEffectBus,
    private val consumer: EffectsConsumer,
    private val sawTickHolder: InvokersFactoryImpl.SawTickHolder
) : SystemInvoker {
    override fun invoke() {
        effectBus.effects().forEach(::process)
    }

    private fun process(effect: CottaEffect) {
        sawTickHolder.tick = effectBus.getTickForEffect(effect)
        consumer.handleEffect(effect)
        sawTickHolder.tick = null
    }
}
