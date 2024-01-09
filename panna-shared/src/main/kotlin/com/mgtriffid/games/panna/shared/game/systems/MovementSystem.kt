package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect

@Predicted class MovementSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(VelocityComponent::class)) {
            val velocity = e.getComponent(VelocityComponent::class)
            ctx.fire(
                MovementEffect.create(
                    velocity.velX * ctx.clock().delta(),
                    velocity.velY * ctx.clock().delta(),
                    e.id
                )
            )
        }
    }
}
