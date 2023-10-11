package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.EntityShotEffect

class EntityShotTestEffectConsumerSystem(
    private val entities: Entities
): EffectsConsumerSystem {
    override fun handle(e: CottaEffect) {
        if (e is EntityShotEffect) {
            val entity = entities.get(e.entityId)
            if (entity.hasComponent(HealthTestComponent::class)) {
                entity.getComponent(HealthTestComponent::class).health -= 5
            }
        }
    }
}
