package com.mgtriffid.games.cotta.network.kryonet.client

import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues.Issues
import java.util.Queue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface Saver {
    fun <T: Any> save(obj: T, block: (T) -> Unit)
}

internal class SimpleSaver : Saver {
    override fun <T: Any> save(obj: T, block: (T) -> Unit) {
        block(obj)
    }
}

internal class LaggingSaver(
    private val issues: Issues,
    private val impl: Saver
) : Saver {
    private val executors = Executors.newScheduledThreadPool(1)

    override fun <T: Any> save(obj: T, block: (T) -> Unit) {
        if (issues.packetLoss >= Math.random()) {
            return
        }
        executors.schedule(
            { impl.save(obj, block) },
            issues.latency.random(),
            TimeUnit.MILLISECONDS
        )
        impl.save(obj, block)
    }
}
