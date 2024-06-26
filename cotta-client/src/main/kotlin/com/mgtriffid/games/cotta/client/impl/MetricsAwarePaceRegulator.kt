package com.mgtriffid.games.cotta.client.impl

import com.codahale.metrics.MetricRegistry
import com.mgtriffid.games.cotta.client.PaceRegulator

private val logger: mu.KLogger = mu.KotlinLogging.logger {}

// BUG this is not enough. What is observed is that with packet loss 0.05
//  the server_buffer_ahead metric jumps from 0 to a lot and then back to 0
class MetricsAwarePaceRegulator : PaceRegulator {
    override fun localTickLength(
        tickLength: Long,
        metrics: MetricRegistry
    ): Long {
        val histogram = metrics.histogram("server_buffer_ahead").snapshot
        if (histogram.min < 2) {
            return (tickLength * 0.9f).toLong()
        }
        if (histogram.min > 2) {
            return (tickLength * 1.1f).toLong()
        }
        return tickLength
    }

    override fun simulationTickLength(
        tickLength: Long,
        metrics: MetricRegistry
    ): Long {
        val histogram = metrics.histogram("buffer_ahead").snapshot
        if (histogram.min > 2) {
            return (tickLength * 0.9f).toLong()
        }
        if (histogram.min < 2) {
            return (tickLength * 1.1f).toLong()
        }
        return tickLength
    }
}
