package com.mgtriffid.games.cotta.client

import com.codahale.metrics.MetricRegistry

class NoOpPaceRegulatorImpl : PaceRegulator {
    override fun calculate(
        tickLength: Long,
        metrics: MetricRegistry
    ): Long {
        return tickLength
    }
}
