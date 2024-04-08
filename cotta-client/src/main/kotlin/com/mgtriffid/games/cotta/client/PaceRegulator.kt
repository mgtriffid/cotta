package com.mgtriffid.games.cotta.client

import com.codahale.metrics.MetricRegistry

// TODO better name
interface PaceRegulator {
    fun calculate(tickLength: Long, metrics: MetricRegistry): Long
}
