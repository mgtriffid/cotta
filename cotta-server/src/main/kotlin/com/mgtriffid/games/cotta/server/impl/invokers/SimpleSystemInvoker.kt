package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingCottaSystem

class SimpleSystemInvoker(
    private val system: CottaSystem,
    private val state: CottaState,
    private val effectBus: EffectBus
) : SystemInvoker {
    override fun invoke() {
        if (system is EntityProcessingCottaSystem) {
            for (entity in state.entities().all()) {
                system.update(entity)
            }
        }
        if (system is EffectsConsumer) {
            effectBus.effects().forEach {
                system.handleEffect(it)
            }
        }
    }
}
