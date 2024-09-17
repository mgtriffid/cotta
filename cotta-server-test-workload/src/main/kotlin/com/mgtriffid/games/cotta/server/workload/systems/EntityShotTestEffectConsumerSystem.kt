package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.EntityShotEffect

class EntityShotTestEffectConsumerSystem : EffectsConsumerSystem<EntityShotEffect> {
    override val effectType: Class<EntityShotEffect> = EntityShotEffect::class.java
    override fun handle(e: EntityShotEffect, ctx: EffectProcessingContext) {
        val entity = ctx.entities().get(e.entityId) ?: return
        if (entity.hasComponent(HealthTestComponent::class)) {
            entity.getComponent(HealthTestComponent::class).health -= 5
        }
    }
}
