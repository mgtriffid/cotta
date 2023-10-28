package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext

interface EffectsConsumerSystem : CottaSystem {
    fun handle(e: CottaEffect)
}
