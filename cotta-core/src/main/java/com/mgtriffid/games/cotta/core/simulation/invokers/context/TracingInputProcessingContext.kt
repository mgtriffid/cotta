package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface TracingInputProcessingContext : InputProcessingContext {
    fun setTrace(trace: CottaTrace?)
    fun getTrace(): CottaTrace?
}
