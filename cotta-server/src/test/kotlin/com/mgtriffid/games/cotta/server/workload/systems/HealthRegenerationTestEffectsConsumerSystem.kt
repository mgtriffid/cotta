package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationTestEffect

class HealthRegenerationTestEffectsConsumerSystem(private val entities: Entities) : EffectsConsumerSystem {
    override fun handle(e: CottaEffect) {
        if (e is HealthRegenerationTestEffect) {
            entities.get(e.entityId).getComponent(HealthTestComponent::class).health += e.health
        }
    }
}
