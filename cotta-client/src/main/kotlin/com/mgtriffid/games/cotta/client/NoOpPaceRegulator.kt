package com.mgtriffid.games.cotta.client

import com.codahale.metrics.MetricRegistry

class NoOpPaceRegulator : PaceRegulator {
    override fun localTickLength(
        tickLength: Long,
        metrics: MetricRegistry
    ): Long {
        return tickLength
    }

    override fun simulationTickLength(
        tickLength: Long,
        metrics: MetricRegistry
    ): Long {
        return tickLength
    }
}
