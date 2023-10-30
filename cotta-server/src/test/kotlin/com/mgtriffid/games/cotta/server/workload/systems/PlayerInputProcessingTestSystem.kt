package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.cotta.server.workload.components.PlayerInputTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect

class PlayerInputProcessingTestSystem : InputProcessingSystem {

    override fun process(e: Entity, ctx: InputProcessingContext) {
        if (e.inputComponents().contains(PlayerInputTestComponent::class)) {
            val input = e.getInputComponent(PlayerInputTestComponent::class)
            if (input.shoot) {
                ctx.fire(ShotFiredTestEffect(x = input.aim))
            }
        }
    }
}
