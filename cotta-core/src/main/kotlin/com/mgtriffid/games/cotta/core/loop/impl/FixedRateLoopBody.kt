package com.mgtriffid.games.cotta.core.loop.impl

import com.mgtriffid.games.cotta.core.loop.LoopBody

private val logger = mu.KotlinLogging.logger {}

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
        (nextTickAt - System.currentTimeMillis()).takeIf { it > 0 }?.let {
            logger.debug { "Sleeping for $it millis" }
            Thread.sleep(it)
        } ?: logger.warn { "Tick took too long, skipping sleep" }
    }
}
