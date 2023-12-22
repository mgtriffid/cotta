package com.mgtriffid.games.cotta.core.simulation

import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface EffectsHistory {
    fun forTick(tick: Long): Collection<CottaEffect>
    fun record(effect: CottaEffect, sawTick: Long?, tick: Long)
}