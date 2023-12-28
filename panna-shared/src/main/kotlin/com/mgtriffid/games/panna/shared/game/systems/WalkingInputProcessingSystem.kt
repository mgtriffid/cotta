package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import com.mgtriffid.games.panna.shared.game.components.input.*
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect

private val logger = mu.KotlinLogging.logger {}

@Predicted class WalkingInputProcessingSystem : InputProcessingSystem {
    override fun process(e: Entity, ctx: InputProcessingContext) {
        if (e.hasInputComponent(WalkingInputComponent::class) && e.hasComponent(WalkingComponent::class)) {
            val input = e.getInputComponent(WalkingInputComponent::class)
            val walking = e.getComponent(WalkingComponent::class)
            if (input.direction == WALKING_DIRECTION_NONE) {
                return
            }
            val (x, y) = when (input.direction) {
                WALKING_DIRECTION_UP -> Pair(0, walking.speed)
                WALKING_DIRECTION_DOWN -> Pair(0, -walking.speed)
                WALKING_DIRECTION_LEFT -> Pair(-walking.speed, 0)
                WALKING_DIRECTION_RIGHT -> Pair(walking.speed, 0)
                WALKING_DIRECTION_NONE -> throw IllegalStateException("Should have been handled above")
                else -> throw IllegalStateException("Unknown direction: ${input.direction}")
            }
            ctx.fire(MovementEffect.create(x, y, e.id))
        }
    }
}
