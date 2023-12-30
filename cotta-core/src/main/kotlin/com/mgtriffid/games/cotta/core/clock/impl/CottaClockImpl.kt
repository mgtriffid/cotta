package com.mgtriffid.games.cotta.core.clock.impl

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.entities.TickProvider

class CottaClockImpl(
    private val tickProvider: TickProvider,
    private val tickLength: Long
) : CottaClock {
    override fun time(): Long {
        return tickProvider.tick * tickLength
    }
}