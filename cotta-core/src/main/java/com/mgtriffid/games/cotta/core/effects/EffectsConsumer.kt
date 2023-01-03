package com.mgtriffid.games.cotta.core.effects

import com.mgtriffid.games.cotta.core.systems.CottaSystem

interface EffectsConsumer : CottaSystem {
    fun handleEffect(e: CottaEffect)
}
