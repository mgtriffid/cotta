package com.mgtriffid.games.panna.shared.game.systems.shooting

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.ShootComponent
import com.mgtriffid.games.panna.shared.game.effects.shooting.createShootEffect

@Predicted
class ShootingProcessingSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(ShootComponent::class)) {
            val shoot = e.getComponent(ShootComponent::class)
            if (shoot.isShooting) {
                ctx.fire(createShootEffect(e.id))
            }
        }
    }
}
