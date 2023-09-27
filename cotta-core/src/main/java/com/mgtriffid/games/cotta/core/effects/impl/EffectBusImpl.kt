package com.mgtriffid.games.cotta.core.effects.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectPublisher

class EffectBusImpl : EffectBus {
    val effects = ArrayList<CottaEffect>()

    override fun effects(): Collection<CottaEffect> {
        return ArrayList(effects)
    }

    override fun clear() {
        effects.clear()
    }

    override fun publisher() = object : EffectPublisher {
        override fun fire(effect: CottaEffect) {
            effects.add(effect)
        }
    }
}
