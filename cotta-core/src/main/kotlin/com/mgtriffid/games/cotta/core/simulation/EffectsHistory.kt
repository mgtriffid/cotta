package com.mgtriffid.games.cotta.core.simulation

import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface EffectsHistory {
    fun record(effect: CottaEffect, sawTick: Long?, tick: Long)
}
