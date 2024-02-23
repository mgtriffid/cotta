package com.mgtriffid.games.cotta.network.kryonet.client

import com.mgtriffid.games.cotta.core.config.DebugConfig

internal fun DebugConfig.EmulatedNetworkConditions.WithIssues.Latency.random(): Long {
    return (min..max).random()
}
