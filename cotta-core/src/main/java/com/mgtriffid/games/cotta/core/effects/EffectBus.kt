package com.mgtriffid.games.cotta.core.effects

import com.mgtriffid.games.cotta.core.effects.impl.EffectBusImpl

interface EffectBus {

    companion object {
        fun getInstance(): EffectBus = EffectBusImpl()
    }

    fun effects(): Collection<CottaEffect>

    fun clear()

    fun publisher() : EffectPublisher
}
