package com.mgtriffid.games.cotta.network.kryonet.client

import com.mgtriffid.games.cotta.core.config.DebugConfig
import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues.Issues
import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues.Latency
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import java.util.Queue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal interface Saver {
    fun save(obj: ServerToClientDto, packetsQueue: Queue<ServerToClientDto>)
}

internal class SimpleSaver : Saver {
    override fun save(obj: ServerToClientDto, packetsQueue: Queue<ServerToClientDto>) {
        packetsQueue.add(obj)
    }
}

internal class LaggingSaver(
    private val issues: Issues,
    private val impl: Saver
) : Saver {
    private val executors = Executors.newScheduledThreadPool(1)

    override fun save(obj: ServerToClientDto, packetsQueue: Queue<ServerToClientDto>) {
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
