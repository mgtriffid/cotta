package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.physics.GravityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import kotlin.math.max

// Mutates component outside of effect processing. Is it even possible?
@Predicted class GravitySystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(GravityComponent::class) && e.hasComponent(VelocityComponent::class)) {
            if (e.hasComponent(JumpingComponent::class) && !e.getComponent(JumpingComponent::class).inAir) {
                return // not affected by gravity while standing
            }
            val velocityComponent = e.getComponent(VelocityComponent::class)
            // TODO move that max Y vel down to the Gravity Component: air resistance is different for different things.
            velocityComponent.velY = max(velocityComponent.velY - 500 * ctx.clock().delta(), -1200f)
        }
    }
}
