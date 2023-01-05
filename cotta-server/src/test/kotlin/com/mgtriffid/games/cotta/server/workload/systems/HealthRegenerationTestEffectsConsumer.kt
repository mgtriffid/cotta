package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationTestEffect

// TODO smarter DI
class HealthRegenerationTestEffectsConsumer(private val entities: Entities) : EffectsConsumer {
    override fun handleEffect(e: CottaEffect) {
        if (e is HealthRegenerationTestEffect) {
            entities.get(e.entityId).getComponent(HealthTestComponent::class).health += e.health
        }
    }
}
