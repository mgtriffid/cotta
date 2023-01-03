package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingCottaSystem
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationEffect

class RegenerationSystem(val effectBus: EffectBus) : EntityProcessingCottaSystem {
    override fun update(e: Entity) {
        if (e.hasComponent(HealthTestComponent::class)) {
            effectBus.fire(HealthRegenerationEffect(e.id, 1))
        }
    }
}
