package com.mgtriffid.games.cotta.core.effects

interface EffectBus {

    fun effects(): Collection<CottaEffect>

    fun clear()

    fun publisher(): EffectPublisher
}
