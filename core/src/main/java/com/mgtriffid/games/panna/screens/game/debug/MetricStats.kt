package com.mgtriffid.games.panna.screens.game.debug

import com.codahale.metrics.Histogram

class MetricStats(
    val min: Double,
    val max: Double,
    val avg: Double,
    val stdDev: Double
) {
    override fun toString(): String {
        return "min: ${String.format("%.2f", min)}," +
            " max: ${String.format("%.2f", max)}," +
            "avg: ${String.format("%.2f", avg)}," +
            " stdDev: ${String.format("%.2f", stdDev)}"
    }
}

fun Histogram.toMetricStats(): MetricStats {
    val snapshot = snapshot
    return MetricStats(
        snapshot.min.toDouble(),
        snapshot.max.toDouble(),
        snapshot.mean,
        snapshot.stdDev
    )
}
