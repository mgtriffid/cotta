package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.effects.EffectsConsumer

class LagCompensatingEffectsConsumerInvoker(
    private val effectBus: LagCompensatingEffectBus,
    private val effectsConsumer: EffectsConsumer,
    private val sawTickHolder: LagCompensatingInputProcessingSystemInvoker.SawTickHolder
) : SystemInvoker {
    override fun invoke() {
        effectBus.effects().forEach { effect ->
            sawTickHolder.tick = effectBus.getTickForEffect(effect)
            effectsConsumer.handleEffect(effect)
            sawTickHolder.tick = null
        }
    }
}
