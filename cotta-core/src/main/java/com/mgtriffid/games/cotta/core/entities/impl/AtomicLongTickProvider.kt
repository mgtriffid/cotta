package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.TickProvider
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger {}

class AtomicLongTickProvider: TickProvider {
    private val tickHolder = AtomicLong()

    override var tick: Long
        get() = tickHolder.get()
        set(value) {
            logger.debug { "Tick updated to $value" }
            tickHolder.set(value)
        }
}
