package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent.Companion.ORIENTATION_LEFT
import com.mgtriffid.games.panna.shared.game.components.PositionComponent.Companion.ORIENTATION_RIGHT
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect
import kotlin.math.roundToInt

private val logger = mu.KotlinLogging.logger {}

@Predicted class MovementEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is MovementEffect) {
            logger.debug { "Received MovementEffect: $e" }
            val entity = ctx.entities().get(e.entityId)
            if (entity.hasComponent(PositionComponent::class)) {
                val position = entity.getComponent(PositionComponent::class)
                position.xPos += (e.velocityX * ctx.clock().delta()).roundToInt()
                position.yPos += (e.velocityY * ctx.clock().delta()).roundToInt()
                position.orientation = if (e.velocityX > 0) ORIENTATION_RIGHT else ORIENTATION_LEFT
                logger.debug { "position.xPos = ${position.xPos}" }
                if (position.xPos < -50 || position.xPos > 1000 || position.yPos < -100 || position.yPos > 2000) {
                    ctx.entities().remove(e.entityId)
                }
            }
        }
    }
}
