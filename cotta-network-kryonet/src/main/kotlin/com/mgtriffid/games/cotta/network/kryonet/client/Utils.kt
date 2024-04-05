package com.mgtriffid.games.cotta.network.kryonet.client

import com.mgtriffid.games.cotta.core.config.DebugConfig
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

internal fun DebugConfig.EmulatedNetworkConditions.WithIssues.Latency.random(): Long {
    return (min..max).random()
}

fun daemonScheduledExecutorService(): ScheduledExecutorService =
    Executors.newScheduledThreadPool(
        1
    ) { r ->
        Executors.defaultThreadFactory().newThread(r).apply {
            isDaemon = true
        }
    }
