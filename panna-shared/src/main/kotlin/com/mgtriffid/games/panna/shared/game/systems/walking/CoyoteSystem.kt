package com.mgtriffid.games.panna.shared.game.systems.walking

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SolidTerrainComponent
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent

@Predicted
class CoyoteSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (
            e.hasComponent(ColliderComponent::class) &&
            e.hasComponent(JumpingComponent::class) &&
            e.hasComponent(PositionComponent::class)
        ) {
            val entityCollider: ColliderComponent = e.getComponent(ColliderComponent::class)
            val jumpingComponent = e.getComponent(JumpingComponent::class)
            val entityPosition = e.getComponent(PositionComponent::class)
            if (!jumpingComponent.inAir) {
                val blocksToStandOn = ctx.entities().all()
                    .filter {
                        it.hasComponent(ColliderComponent::class) &&
                            it.hasComponent(SolidTerrainComponent::class) &&
                            it.hasComponent(PositionComponent::class)
                    }.filter {
                        val collider = it.getComponent(ColliderComponent::class)
                        val position = it.getComponent(PositionComponent::class)
                        val xDiff = position.xPos - entityPosition.xPos
                        val yDiff = entityPosition.yPos - position.yPos
                        val xOverlap = (collider.width + entityCollider.width) / 2 - Math.abs(xDiff)
                        val yOverlap = (collider.height + entityCollider.height) / 2 - yDiff
                        xOverlap > 0 && yOverlap > -0.001 && yOverlap < 0.001
                    }
                if (blocksToStandOn.isEmpty()) {
                    jumpingComponent.inAir = true
                }
            }
        }
    }
}
