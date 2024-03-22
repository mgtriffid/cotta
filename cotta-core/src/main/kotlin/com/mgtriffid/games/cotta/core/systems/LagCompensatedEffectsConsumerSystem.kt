package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.PlayerId

interface LagCompensatedEffectsConsumerSystem<T: CottaEffect> : EffectsConsumerSystem<T>  {
    fun player(effect: T) : PlayerId
}
