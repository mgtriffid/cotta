package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_DOWN
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_LEFT
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_RIGHT
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_UP
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect

class MovementEffectConsumer(private val entities: Entities) : EffectsConsumer {
    override fun handleEffect(e: CottaEffect) {
        if (e is MovementEffect) {
            val entity = entities.get(e.entityId)
            if (entity.hasComponent(PositionComponent::class)) {
                val position = entity.getComponent(PositionComponent::class)
                position.xPos = when (e.direction) {
                    WALKING_DIRECTION_LEFT -> position.xPos - e.velocity
                    WALKING_DIRECTION_RIGHT -> position.xPos + e.velocity
                    else -> position.xPos
                }
                position.yPos = when (e.direction) {
                    WALKING_DIRECTION_UP -> position.yPos + e.velocity
                    WALKING_DIRECTION_DOWN -> position.yPos - e.velocity
                    else -> position.yPos
                }
            }
        }
    }
}
