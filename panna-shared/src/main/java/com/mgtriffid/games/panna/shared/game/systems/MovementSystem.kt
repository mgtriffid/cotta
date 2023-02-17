package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.MovementDirection
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent

class MovementSystem : EntityProcessingSystem {
    override fun update(e: Entity) {
/*
        if (e.hasComponent(WalkingComponent::class) && e.hasComponent(PositionComponent::class)) {
            val position = e.getComponent(PositionComponent::class)
            val walking = e.getComponent(WalkingComponent::class)
            position.orientation = walking.movementDirection.let {
                when (it) {
                    MovementDirection.IDLE -> position.orientation
                    MovementDirection.RIGHT -> PositionComponent.Orientation.RIGHT
                    MovementDirection.LEFT -> PositionComponent.Orientation.LEFT
                }
            }
        }
*/
    }
}
