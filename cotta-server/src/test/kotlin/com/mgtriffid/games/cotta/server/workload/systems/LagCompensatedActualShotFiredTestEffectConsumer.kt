package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.LagCompensatedEffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.ActualShotFiredEffect
import com.mgtriffid.games.cotta.server.workload.effects.createEntityShotEffect

class LagCompensatedActualShotFiredTestEffectConsumer :
    LagCompensatedEffectsConsumerSystem<ActualShotFiredEffect> {
    override val effectType: Class<ActualShotFiredEffect> = ActualShotFiredEffect::class.java
    override fun handle(e: ActualShotFiredEffect, ctx: EffectProcessingContext) {
        ctx.entities().all().filter {
            it.hasComponent(LinearPositionTestComponent::class)
                    && it.getComponent(LinearPositionTestComponent::class).x == e.x
        }.forEach {
            ctx.fire(createEntityShotEffect(it.id))
        }
    }

    override fun player(effect: ActualShotFiredEffect): PlayerId {
        return effect.shooter
    }
}
