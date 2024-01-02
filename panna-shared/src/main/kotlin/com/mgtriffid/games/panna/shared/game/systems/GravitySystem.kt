package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent

@Predicted class GravitySystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(JumpingComponent::class) && e.hasComponent(VelocityComponent::class)) {
            val jumpingComponent = e.getComponent(JumpingComponent::class)
            val velocityComponent = e.getComponent(VelocityComponent::class)
            if (jumpingComponent.inAir) {
                velocityComponent.velY -= 50
            }
        }
    }
}