package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.annotations.LagCompensated
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.ActualShotFiredEffect
import com.mgtriffid.games.cotta.server.workload.effects.EntityShotEffect
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect

@LagCompensated
class LagCompensatedActualShotFiredTestEffectConsumer : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is ActualShotFiredEffect) {
            ctx.entities().all().filter {
                it.hasComponent(LinearPositionTestComponent::class)
                        && it.getComponent(LinearPositionTestComponent::class).x == e.x
            }.forEach {
                ctx.fire(EntityShotEffect(it.id))
            }
        }
    }
}
