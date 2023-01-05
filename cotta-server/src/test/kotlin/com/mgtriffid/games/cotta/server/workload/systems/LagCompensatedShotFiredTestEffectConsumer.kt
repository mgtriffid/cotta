package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.annotations.LagCompensated
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.EntityShotEffect
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect

class LagCompensatedShotFiredTestEffectConsumer(
    private val effectBus: EffectBus,
    @LagCompensated private val entities: Entities
) : EffectsConsumer {
    override fun handleEffect(e: CottaEffect) {
        if (e is ShotFiredTestEffect) {
            entities.all().filter {
                it.hasComponent(LinearPositionTestComponent::class)
                        && it.getComponent(LinearPositionTestComponent::class).x == e.x
            }.forEach {
                effectBus.fire(EntityShotEffect(it.id))
            }
        }
    }
}
