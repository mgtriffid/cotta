package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_NONE
import com.mgtriffid.games.panna.shared.game.components.input.WalkingInputComponent
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect

private val logger = mu.KotlinLogging.logger {}
class WalkingInputProcessingSystem : InputProcessingSystem {
    override fun process(e: Entity, ctx: InputProcessingContext) {
        if (e.hasInputComponent(WalkingInputComponent::class) && e.hasComponent(WalkingComponent::class)) {
            val input = e.getInputComponent(WalkingInputComponent::class)
            val walking = e.getComponent(WalkingComponent::class)
            if (input.direction == WALKING_DIRECTION_NONE) {
                return
            }
            ctx.fire(MovementEffect.create(input.direction, walking.speed, e.id).also {
                logger.debug { "MovementEffect fired: $it" }
            })
        }
    }
}
