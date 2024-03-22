package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationTestEffect

class HealthRegenerationTestEffectsConsumerSystem : EffectsConsumerSystem<HealthRegenerationTestEffect> {
    override val effectType: Class<HealthRegenerationTestEffect> = HealthRegenerationTestEffect::class.java
    override fun handle(e: HealthRegenerationTestEffect, ctx: EffectProcessingContext) {
        ctx.entities().get(e.entityId)?.let {
            it.getComponent(HealthTestComponent::class).health += e.health
        }
    }
}
