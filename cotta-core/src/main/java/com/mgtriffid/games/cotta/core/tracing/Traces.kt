package com.mgtriffid.games.cotta.core.tracing

import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface Traces {
    fun set(effect: CottaEffect, trace: CottaTrace)
    fun get(effect: CottaEffect): CottaTrace
}
