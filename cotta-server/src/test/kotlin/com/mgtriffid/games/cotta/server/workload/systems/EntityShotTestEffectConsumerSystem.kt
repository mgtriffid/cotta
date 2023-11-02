package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.EntityShotEffect

class EntityShotTestEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is EntityShotEffect) {
            val entity = ctx.entities().get(e.entityId)
            if (entity.hasComponent(HealthTestComponent::class)) {
                entity.getComponent(HealthTestComponent::class).health -= 5
            }
        }
    }
}
