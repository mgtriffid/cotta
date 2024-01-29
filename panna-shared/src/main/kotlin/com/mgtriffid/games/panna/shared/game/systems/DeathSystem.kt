package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.HealthComponent

class DeathSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(HealthComponent::class)) {
            val health = e.getComponent(HealthComponent::class)
             if (health.health <= 0) {
                 entityDies(e, ctx)
             }
        }
    }

    private fun entityDies(e: Entity, ctx: EntityProcessingContext) {
        val entities = ctx.entities()
        entities.remove(e.id)
    }
}
