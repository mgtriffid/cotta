package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectPublisher
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.EntityShotEffect
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect

class ShotFiredTestEffectConsumerSystem(
    private val effectPublisher: EffectPublisher,
    private val entities: Entities
) : EffectsConsumerSystem {
    override fun handle(e: CottaEffect) {
        if (e is ShotFiredTestEffect) {
            entities.all().filter {
                it.hasComponent(LinearPositionTestComponent::class)
                        && it.getComponent(LinearPositionTestComponent::class).x == e.x
            }.forEach {
                effectPublisher.fire(EntityShotEffect(it.id))
            }
        }
    }
}
