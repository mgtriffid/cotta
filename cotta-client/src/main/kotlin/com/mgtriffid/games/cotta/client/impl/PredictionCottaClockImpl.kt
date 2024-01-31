package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.entities.TickProvider

class PredictionCottaClockImpl(
    private val predictionTickProvider: TickProvider,
    private val tickLength: Long,
) : CottaClock {
    private val tickLengthSeconds = tickLength / 1000f
    var lagBehind: Long = 0

    override fun time(): Long {
        return (predictionTickProvider.tick + lagBehind) * tickLength
    }

    override fun delta(): Float {
        return tickLengthSeconds
    }
}
