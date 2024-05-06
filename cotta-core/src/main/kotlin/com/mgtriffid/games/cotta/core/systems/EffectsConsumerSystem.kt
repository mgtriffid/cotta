package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext

/**
 * A system that consumes effects.
 *
 * @see LagCompensatedEffectsConsumerSystem
 */
interface EffectsConsumerSystem<T: CottaEffect> : CottaSystem {

    // TODO scan, don't force dev to do this. It's redundant.
    /**
     * Override this to specify the effect
     */
    val effectType: Class<T>

    /**
     * Handle the effect. Is called for each Effect published to the
     * [com.mgtriffid.games.cotta.core.effects.EffectBus] in the same tick
     * before this system is invoked.
     */
    fun handle(e: T, ctx: EffectProcessingContext)
}
