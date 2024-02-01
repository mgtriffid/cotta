package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.effects.ActualShotFiredEffect
import com.mgtriffid.games.cotta.server.workload.effects.ShotFiredTestEffect

class StepOneShotFiredTestEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is ShotFiredTestEffect) {
            ctx.fire(ActualShotFiredEffect(x = e.x))
        }
    }
}
