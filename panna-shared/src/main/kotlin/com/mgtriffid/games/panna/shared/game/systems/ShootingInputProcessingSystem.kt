package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.input.ShootInputComponent
import com.mgtriffid.games.panna.shared.game.effects.shooting.ShootEffect
import com.mgtriffid.games.panna.shared.game.effects.shooting.createShootEffect

@Predicted class ShootingInputProcessingSystem : InputProcessingSystem {
    override fun process(e: Entity, ctx: InputProcessingContext) {
        if (e.hasInputComponent(ShootInputComponent::class)) {
            val shootInput = e.getInputComponent(ShootInputComponent::class)
            if (shootInput.isShooting) {
                ctx.fire(
                    createShootEffect(e.id)
                )
            }
        }
    }
}
