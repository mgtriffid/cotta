package com.mgtriffid.games.cotta.network.kryonet.client

import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues.Issues
import java.util.Queue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal interface Saver {
    fun <T: Any> save(obj: T, packetsQueue: Queue<T>)
}

internal class SimpleSaver : Saver {
    override fun <T: Any> save(obj: T, packetsQueue: Queue<T>) {
        packetsQueue.add(obj)
    }
}

internal class LaggingSaver(
    private val issues: Issues,
    private val impl: Saver
) : Saver {
    private val executors = Executors.newScheduledThreadPool(1)

    override fun <T: Any> save(obj: T, packetsQueue: Queue<T>) {
        if (issues.packetLoss >= Math.random()) {
            return
        }
        executors.schedule(
            { impl.save(obj, packetsQueue) },
            issues.latency.random(),
            TimeUnit.MILLISECONDS
        )
        impl.save(obj, packetsQueue)
    }
}
