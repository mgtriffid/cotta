package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectPublisher
import jakarta.inject.Inject

// TODO better naming
// TODO better placing
interface LagCompensatingEffectBus : EffectBus {
    fun getTickForEffect(effect: CottaEffect): Long?
}

class LagCompensatingEffectBusImpl @Inject constructor(
    private val effectBus: EffectBus,
    private val sawTickHolder: SawTickHolder
): LagCompensatingEffectBus {
    private val ticksForEffects: MutableMap<CottaEffect, Long?> = HashMap()

    override fun effects(): Collection<CottaEffect> {
        return effectBus.effects()
    }

    override fun clear() {
        effectBus.clear()
        ticksForEffects.clear()
    }

    override fun getTickForEffect(effect: CottaEffect) = ticksForEffects[effect]

    override fun publisher(): EffectPublisher {
        val publisher = effectBus.publisher()
        return object : EffectPublisher {
            override fun fire(effect: CottaEffect) {
                ticksForEffects[effect] = sawTickHolder.tick
                publisher.fire(effect)
            }
        }
    }
}
