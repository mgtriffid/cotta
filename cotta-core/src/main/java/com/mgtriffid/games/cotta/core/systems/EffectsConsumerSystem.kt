package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface EffectsConsumerSystem : CottaSystem {
    fun handle(e: CottaEffect)
}
