package com.mgtriffid.games.panna.shared.game.systems.shooting

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.HealthComponent
import com.mgtriffid.games.panna.shared.game.effects.shooting.BulletHitsDudeEffect
import com.mgtriffid.games.panna.shared.game.effects.shooting.RailgunHitsDudeEffect

class RailgunHitsDudeEffectConsumer : EffectsConsumerSystem<RailgunHitsDudeEffect> {
    override val effectType: Class<RailgunHitsDudeEffect> = RailgunHitsDudeEffect::class.java
    override fun handle(e: RailgunHitsDudeEffect, ctx: EffectProcessingContext) {
        ctx.entities().get(e.shotId)?.let { entity ->
            val healthComponent = entity.getComponent(HealthComponent::class)
            healthComponent.health -= 40
        }
    }
}
