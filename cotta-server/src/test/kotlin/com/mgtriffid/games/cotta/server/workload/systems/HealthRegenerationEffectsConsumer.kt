package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationEffect

// TODO smarter DI
class HealthRegenerationEffectsConsumer(private val state: CottaState) : EffectsConsumer {
    override fun handleEffect(e: CottaEffect) {
        if (e is HealthRegenerationEffect) {
            state.entities().get(e.entityId).getComponent(HealthTestComponent::class).health += e.health
        }
    }
}
