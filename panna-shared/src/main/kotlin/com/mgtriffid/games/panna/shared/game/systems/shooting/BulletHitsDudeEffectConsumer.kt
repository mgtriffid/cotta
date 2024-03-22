package com.mgtriffid.games.panna.shared.game.systems.shooting

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.HealthComponent
import com.mgtriffid.games.panna.shared.game.effects.shooting.BulletHitsDudeEffect

class BulletHitsDudeEffectConsumer : EffectsConsumerSystem<BulletHitsDudeEffect> {
    override val effectType: Class<BulletHitsDudeEffect> = BulletHitsDudeEffect::class.java
    override fun handle(e: BulletHitsDudeEffect, ctx: EffectProcessingContext) {
        ctx.entities().get(e.shotId)?.let { entity ->
            val healthComponent = entity.getComponent(HealthComponent::class)
            healthComponent.health -= 7
        }
    }
}
