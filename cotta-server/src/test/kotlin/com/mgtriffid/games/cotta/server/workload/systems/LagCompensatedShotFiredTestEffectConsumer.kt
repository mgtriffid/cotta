package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.LagCompensatedEffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect
import com.mgtriffid.games.cotta.server.workload.effects.createEntityShotEffect

class LagCompensatedShotFiredTestEffectConsumer : LagCompensatedEffectsConsumerSystem<ShotFiredTestEffect> {
    override val effectType: Class<ShotFiredTestEffect> = ShotFiredTestEffect::class.java
    override fun handle(e: ShotFiredTestEffect, ctx: EffectProcessingContext) {
        ctx.entities().all().filter {
            it.hasComponent(LinearPositionTestComponent::class)
                    && it.getComponent(LinearPositionTestComponent::class).x == e.x
        }.forEach {
            ctx.fire(createEntityShotEffect(it.id))
        }
    }

    override fun player(effect: ShotFiredTestEffect): PlayerId {
        return effect.shooter
    }
}
