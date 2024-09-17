package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.systems.LagCompensatedEffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.effects.ActualShotFiredEffect
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect
import com.mgtriffid.games.cotta.server.workload.effects.createActualShotFiredEffect

class StepOneShotFiredTestEffectConsumerSystem : LagCompensatedEffectsConsumerSystem<ShotFiredTestEffect> {
    override val effectType: Class<ShotFiredTestEffect> = ShotFiredTestEffect::class.java
    override fun handle(e: ShotFiredTestEffect, ctx: EffectProcessingContext) {
        ctx.fire(createActualShotFiredEffect(x = e.x, shooter = e.shooter))
    }

    override fun player(effect: ShotFiredTestEffect): PlayerId {
        return effect.shooter
    }
}
