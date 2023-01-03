package com.mgtriffid.games.cotta.core.effects.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus

class EffectBusImpl : EffectBus {
    val effects = ArrayList<CottaEffect>()

    override fun fire(effect: CottaEffect) {
        effects.add(effect)
    }

    override fun effects(): Collection<CottaEffect> {
        return ArrayList(effects)
    }

    override fun clear() {
        effects.clear()
    }
}
