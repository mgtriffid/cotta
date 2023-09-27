package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectPublisher
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.cotta.server.workload.components.PlayerInputTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect

class PlayerInputProcessingTestSystem(private val effectPublisher: EffectPublisher) : InputProcessingSystem {

    override fun process(e: Entity) {
        if (e.inputComponents().contains(PlayerInputTestComponent::class)) {
            val input = e.getInputComponent(PlayerInputTestComponent::class)
            if (input.shoot) {
                effectPublisher.fire(ShotFiredTestEffect(x = input.aim))
            }
        }
    }
}
