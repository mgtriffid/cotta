package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import com.mgtriffid.games.panna.shared.game.components.input.WalkingInputComponent
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect

class WalkingInputProcessingSystem(private val effectBus: EffectBus) : InputProcessingSystem {
    override fun process(e: Entity) {
        if (e.hasInputComponent(WalkingInputComponent::class) && e.hasComponent(WalkingComponent::class)) {
            val input = e.getInputComponent(WalkingInputComponent::class)
            val walking = e.getComponent(WalkingComponent::class)
            effectBus.fire(MovementEffect(input.direction, walking.speed, e.id))
        }
    }
}
