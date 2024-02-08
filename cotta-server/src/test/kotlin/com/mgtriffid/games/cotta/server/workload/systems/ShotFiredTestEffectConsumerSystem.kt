package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.EntityShotEffect
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect
import com.mgtriffid.games.cotta.server.workload.effects.createEntityShotEffect

class ShotFiredTestEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is ShotFiredTestEffect) {
            ctx.entities().all().filter {
                it.hasComponent(LinearPositionTestComponent::class)
                        && it.getComponent(LinearPositionTestComponent::class).x == e.x
            }.forEach {
                ctx.fire(createEntityShotEffect(it.id))
            }
        }
    }
}
