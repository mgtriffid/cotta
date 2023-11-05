package com.mgtriffid.games.cotta.core.tracing

interface Tracing {
    fun setTrace(trace: CottaTrace)
    fun reset()

    fun withTrace(trace: CottaTrace, block: () -> Unit) {
        setTrace(trace)
        block()
        reset()
    }
}
