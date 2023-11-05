package com.mgtriffid.games.cotta.core.tracing.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.Traces

class TracesImpl : Traces {
    private val data = HashMap<CottaEffect, CottaTrace>()
    override fun set(effect: CottaEffect, trace: CottaTrace) {
        data[effect] = trace
    }

    override fun get(effect: CottaEffect): CottaTrace {
        return data[effect] ?: throw IllegalStateException("Not found trace for effect $effect")
    }
}
