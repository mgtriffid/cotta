package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface TracingEffectProcessingContext : EffectProcessingContext {
    fun setTrace(trace: CottaTrace?)
    fun getTrace(): CottaTrace?
}