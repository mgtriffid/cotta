package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface EffectsConsumer : CottaSystem {
    fun handleEffect(e: CottaEffect)
}
