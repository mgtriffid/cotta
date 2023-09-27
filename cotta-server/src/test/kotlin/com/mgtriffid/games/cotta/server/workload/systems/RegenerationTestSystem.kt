package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectPublisher
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationTestEffect

class RegenerationTestSystem(private val effectPublisher: EffectPublisher) : EntityProcessingSystem {
    override fun process(e: Entity) {
        if (e.hasComponent(HealthTestComponent::class)) {
            effectPublisher.fire(HealthRegenerationTestEffect(e.id, 1))
        }
    }
}
