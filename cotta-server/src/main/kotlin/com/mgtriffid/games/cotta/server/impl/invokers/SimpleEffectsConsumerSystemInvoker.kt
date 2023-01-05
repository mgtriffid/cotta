package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer

class SimpleEffectsConsumerSystemInvoker(
    private val consumer: EffectsConsumer,
    private val effectBus: EffectBus
) : SystemInvoker {
    override fun invoke() {
        effectBus.effects().forEach(::process)
    }

    private fun process(it: CottaEffect) {
        consumer.handleEffect(it)
    }
}
