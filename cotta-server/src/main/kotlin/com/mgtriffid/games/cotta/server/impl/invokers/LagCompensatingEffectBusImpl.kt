package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.server.impl.EffectsHistory

// TODO better naming
// TODO better placing
// TODO separate publisher and the whole EffectBus
interface LagCompensatingEffectBus : EffectBus {
    fun getTickForEffect(effect: CottaEffect): Long?
}

class HistoricalLagCompensatingEffectBus(
    val history: EffectsHistory,
    val impl: LagCompensatingEffectBus,
    val tickProvider: TickProvider
): LagCompensatingEffectBus by impl {
    override fun fire(effect: CottaEffect) {
        impl.fire(effect)
        history.record(effect, impl.getTickForEffect(effect), tickProvider.tick)
    }
}

class LagCompensatingEffectBusImpl(
    private val effectBus: EffectBus,
    private val sawTickHolder: InvokersFactoryImpl.SawTickHolder
): LagCompensatingEffectBus {
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

    override fun getTickForEffect(effect: CottaEffect) = ticksForEffects[effect]
}
