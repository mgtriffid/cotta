package com.mgtriffid.games.panna.shared.game.systems.walking

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_LEFT
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_NONE
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_RIGHT
import com.mgtriffid.games.panna.shared.game.effects.walking.WalkingEffect

@Predicted class WalkingEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is WalkingEffect) {
            val entity = ctx.entities().get(e.entityId)
            entity.getComponent(VelocityComponent::class).apply {
                velX = when (e.direction) {
                    WALKING_DIRECTION_NONE -> 0
                    WALKING_DIRECTION_LEFT -> -1
                    WALKING_DIRECTION_RIGHT -> 1
                    else -> throw IllegalArgumentException("Unknown direction ${e.direction}")
                } * entity.getComponent(WalkingComponent::class).speed
            }
        }
    }
}
