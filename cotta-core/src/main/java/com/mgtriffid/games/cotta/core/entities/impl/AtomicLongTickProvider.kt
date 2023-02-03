package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.TickProvider
import java.util.concurrent.atomic.AtomicLong

class AtomicLongTickProvider: TickProvider {
    private val tickHolder = AtomicLong()

    override var tick: Long
        get() = tickHolder.get()
        set(value) {
            tickHolder.set(value)
        }
}
