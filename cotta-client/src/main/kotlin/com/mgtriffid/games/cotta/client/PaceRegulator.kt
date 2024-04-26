package com.mgtriffid.games.cotta.client

import com.codahale.metrics.MetricRegistry

// TODO better name
interface PaceRegulator {
    fun localTickLength(tickLength: Long, metrics: MetricRegistry): Long
    fun simulationTickLength(tickLength: Long, metrics: MetricRegistry): Long
}
