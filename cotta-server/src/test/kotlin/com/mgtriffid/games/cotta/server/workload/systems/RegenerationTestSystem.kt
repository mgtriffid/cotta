package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationTestEffect

class RegenerationTestSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(HealthTestComponent::class)) {
            ctx.fire(HealthRegenerationTestEffect(e.id, 1))
        }
    }
}
