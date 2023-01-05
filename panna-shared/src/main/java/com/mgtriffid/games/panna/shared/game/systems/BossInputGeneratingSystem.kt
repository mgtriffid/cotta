package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.NonPlayerInputGenerator
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.MovementDirection
import com.mgtriffid.games.panna.shared.game.components.BossInputComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent

@NonPlayerInputGenerator
class BossInputGeneratingSystem: EntityProcessingSystem {
    // processes all Entities that have BossInputComponent on them
    // should set that component
    override fun update(e: Entity) {
        if (e.hasComponent(BossInputComponent::class)) {
            val position = e.getComponent(PositionComponent::class)
            val direction = when {
                position.x > 600 -> MovementDirection.LEFT
                position.x < 200 -> MovementDirection.RIGHT
                position.orientation == PositionComponent.Orientation.LEFT -> MovementDirection.LEFT
                position.orientation == PositionComponent.Orientation.RIGHT -> MovementDirection.RIGHT
                else -> MovementDirection.RIGHT
            }
            val input = e.getComponent(BossInputComponent::class)
            input.movementDirection = direction
            input.jump = false
            input.spellCast = null
        }
    }
}
