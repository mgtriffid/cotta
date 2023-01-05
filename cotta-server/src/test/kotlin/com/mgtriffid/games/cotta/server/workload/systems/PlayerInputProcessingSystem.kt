package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.cotta.server.workload.components.PlayerInputTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect

class PlayerInputProcessingSystem(private val effectBus: EffectBus) : InputProcessingSystem {

    override fun update(e: Entity) {
        if (e.hasComponent(PlayerInputTestComponent::class)) {
            val input = e.getComponent(PlayerInputTestComponent::class)
            if (input.shoot) {
                effectBus.fire(ShotFiredTestEffect(x = input.aim))
            }
        }
    }
}
// when effectBus.fire then it should pass the context from this entity down to the effect.
// context is being created
