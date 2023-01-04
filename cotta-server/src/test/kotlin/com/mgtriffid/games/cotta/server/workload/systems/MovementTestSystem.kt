package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.EntityProcessingCottaSystem
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.components.VelocityTestComponent

class MovementTestSystem : EntityProcessingCottaSystem {
    override fun update(e: Entity) {
        if (
            e.hasComponent(LinearPositionTestComponent::class) &&
            e.hasComponent(VelocityTestComponent::class)
        ) {
            e.getComponent(LinearPositionTestComponent::class).x += e.getComponent(VelocityTestComponent::class).velocity
        }
    }
}
