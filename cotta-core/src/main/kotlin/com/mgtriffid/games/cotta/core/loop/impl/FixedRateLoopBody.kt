package com.mgtriffid.games.cotta.core.loop.impl

import com.mgtriffid.games.cotta.core.loop.LoopBody

class FixedRateLoopBody(
    private val tickLengthMs: Long,
    startsAt: Long,
    private val block: () -> Unit
) : LoopBody {
    private var nextTickAt = startsAt + tickLengthMs
    override fun start() {
        while (true) {
            sleepIfNeeded()
            block()
            nextTickAt += tickLengthMs
        }
    }

    private fun sleepIfNeeded() {
        (nextTickAt - System.currentTimeMillis()).takeIf { it > 0 }?.let { Thread.sleep(it) }
    }
}
