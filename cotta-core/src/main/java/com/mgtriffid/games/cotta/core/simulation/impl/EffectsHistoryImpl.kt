package com.mgtriffid.games.cotta.core.simulation.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import jakarta.inject.Inject
import jakarta.inject.Named

class EffectsHistoryImpl @Inject constructor(
    @Named("historyLength") private val historyLength: Int
): EffectsHistory {
    val data = Array(historyLength) { EffectsHistoryItem() }

    // TODO add guards preventing recording to some old tick
    override fun record(effect: CottaEffect, sawTick: Long?, tick: Long) {
        val historyItem = data[(tick % historyLength).toInt()]
        if (tick != historyItem.tick) {
            historyItem.purge()
            historyItem.tick = tick
        }
        historyItem.effects.add(effect)
        historyItem.sawTicksForEffects[effect] = sawTick
    }

    override fun forTick(tick: Long): Collection<CottaEffect> {
        val item = data[(tick % historyLength).toInt()]
        if (tick != item.tick) return emptyList()
        return item.effects
    }

    class EffectsHistoryItem {
        val effects = ArrayList<CottaEffect>()
        var tick: Long? = null
        val sawTicksForEffects = HashMap<CottaEffect, Long?>()

        fun purge() {
            effects.clear()
            sawTicksForEffects.clear()
        }
    }
}
