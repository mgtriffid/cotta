package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectPublisher
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import jakarta.inject.Inject
import jakarta.inject.Named

// TODO better naming
// TODO better placing
interface LagCompensatingEffectBus : EffectBus {
    fun getTickForEffect(effect: CottaEffect): Long?
}

class HistoricalLagCompensatingEffectBus @Inject constructor(
    val history: EffectsHistory,
    @Named("lagCompensated") val impl: LagCompensatingEffectBus,
    val tickProvider: TickProvider
): LagCompensatingEffectBus by impl {
    override fun publisher(): EffectPublisher {
        val implPublisher = impl.publisher()
        return object : EffectPublisher {
            override fun fire(effect: CottaEffect) {
                implPublisher.fire(effect)
                history.record(effect, impl.getTickForEffect(effect), tickProvider.tick)
            }
        }
    }
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
