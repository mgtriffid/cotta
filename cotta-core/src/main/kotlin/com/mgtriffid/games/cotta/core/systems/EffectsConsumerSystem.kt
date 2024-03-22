package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext

interface EffectsConsumerSystem<T: CottaEffect> : CottaSystem {

    // TODO scan, don't force dev to do this. It's redundant.
    val effectType: Class<T>

    fun handle(e: T, ctx: EffectProcessingContext)
}
