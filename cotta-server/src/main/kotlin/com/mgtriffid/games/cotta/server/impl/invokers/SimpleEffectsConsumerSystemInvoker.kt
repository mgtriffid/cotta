package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.systems.CottaSystem

class SimpleEffectsConsumerSystemInvoker(
    private val system: CottaSystem,
    private val effectBus: EffectBus
) : SystemInvoker {
    override fun invoke() {
        if (system is EffectsConsumer) {
            effectBus.effects().forEach {
                system.handleEffect(it)
            }
        }
    }
}
