package com.mgtriffid.games.panna.shared.game.systems.shooting

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.BulletComponent
import com.mgtriffid.games.panna.shared.game.components.HealthComponent
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent

@Predicted class BulletCollisionSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
/*
        if (e.hasComponent(BulletComponent::class)) {
            ctx.entities().all().filter {
                it.hasComponent(HealthComponent::class) &&
                    it.hasComponent(ColliderComponent::class)
            }
                .filter {  }
        }
*/
    }
}
