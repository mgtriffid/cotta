package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.cotta.server.workload.components.PlayerControlledStubComponent
import com.mgtriffid.games.cotta.server.workload.effects.createShotFiredTestEffect

class PlayerProcessingTestSystem : EntityProcessingSystem {

    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(PlayerControlledStubComponent::class)) {
            val controlled = e.getComponent(PlayerControlledStubComponent::class)
            if (controlled.shoot) {
                ctx.fire(createShotFiredTestEffect(x = controlled.aim, shooter = (e.ownedBy as Entity.OwnedBy.Player).playerId))
            }
        }
    }
}
