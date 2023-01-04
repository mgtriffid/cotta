package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.EntityShotEffect

class EntityShotTestEffectConsumer(
    private val state: CottaState
): EffectsConsumer {
    override fun handleEffect(e: CottaEffect) {
        if (e is EntityShotEffect) {
            val entity = state.entities().get(e.entityId)
            if (entity.hasComponent(HealthTestComponent::class)) {
                entity.getComponent(HealthTestComponent::class).health -= 5
            }
        }
    }
}
