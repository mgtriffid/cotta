package com.mgtriffid.games.cotta.client.impl

import com.codahale.metrics.Histogram
import com.mgtriffid.games.cotta.core.entities.TickProvider
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class IncomingDataBufferMonitor(
    private val incomingDataBuffer: ClientIncomingDataBuffer<*, *>,
    private val globalTick: TickProvider,
    private val bufferHistogram: Histogram
) {
    fun measure() {
        var tick = globalTick.tick
        var count = 0
        while (incomingDataBuffer.simulationInputs.containsKey(tick)) {
            count++
            tick++
        }
        logger.info { "Count: $count" }
        bufferHistogram.update(count)
    }
}
