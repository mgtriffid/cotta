package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.annotations.LagCompensated
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectPublisher
import com.mgtriffid.games.cotta.core.systems.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.EntityShotEffect
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect

class LagCompensatedShotFiredTestEffectConsumer(
    private val effectPublisher: EffectPublisher,
    @LagCompensated private val entities: Entities
) : EffectsConsumer {
    override fun handleEffect(e: CottaEffect) {
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
