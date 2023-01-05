package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus

// TODO better naming
// TODO better placing
// TODO separate publisher and the whole EffectBus
class LagCompensatingEffectBus(
    val effectBus: EffectBus,
    val sawTickHolder: LagCompensatingInputProcessingSystemInvoker.SawTickHolder
): EffectBus {
    private val ticksForEffects: MutableMap<CottaEffect, Long?> = HashMap()

    override fun fire(effect: CottaEffect) {
        ticksForEffects[effect] = sawTickHolder.tick
        effectBus.fire(effect)
    }

    override fun effects(): Collection<CottaEffect> {
        return effectBus.effects()
    }

    override fun clear() {
        effectBus.clear()
        ticksForEffects.clear()
    }

    fun getTickForEffect(effect: CottaEffect) = ticksForEffects[effect]
}
