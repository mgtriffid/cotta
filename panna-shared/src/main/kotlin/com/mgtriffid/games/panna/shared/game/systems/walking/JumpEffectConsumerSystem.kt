package com.mgtriffid.games.panna.shared.game.systems.walking

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.walking.JumpEffect

@Predicted class JumpEffectConsumerSystem : EffectsConsumerSystem<JumpEffect> {
    override val effectType: Class<JumpEffect> = JumpEffect::class.java
    override fun handle(e: JumpEffect, ctx: EffectProcessingContext) {
        val entity = ctx.entities().get(e.entityId) ?: return
        entity.getComponent(VelocityComponent::class).apply {
            val component = entity.getComponent(JumpingComponent::class)
            velY = component.jumpSpeed
            component.inAir = true
        }
    }
}
