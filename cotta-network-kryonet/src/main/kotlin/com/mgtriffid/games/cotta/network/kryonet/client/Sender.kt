package com.mgtriffid.games.cotta.network.kryonet.client

import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues.Issues

interface Sender {
    fun send(obj: Any, block: (Any) -> Unit)
}

internal class SimpleSender : Sender {
    override fun send(obj: Any, block: (Any) -> Unit) {
        block(obj)
    }
}

internal class LaggingSender(
    private val issues: Issues,
    private val impl: Sender
) : Sender {

    private val executors = daemonScheduledExecutorService()

    override fun send(obj: Any, block: (Any) -> Unit) {
        if (issues.packetLoss >= Math.random()) {
            return
        }
        executors.schedule(
            { impl.send(obj, block) },
            issues.latency.random(),
            java.util.concurrent.TimeUnit.MILLISECONDS
        )
    }
}
