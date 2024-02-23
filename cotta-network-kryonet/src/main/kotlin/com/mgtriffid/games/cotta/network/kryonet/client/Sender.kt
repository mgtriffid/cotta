package com.mgtriffid.games.cotta.network.kryonet.client

import com.esotericsoftware.kryonet.Client
import com.mgtriffid.games.cotta.core.config.DebugConfig
import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues.Latency
import java.util.concurrent.Executors

internal interface Sender {
    fun send(client: Client, obj: Any)
}

internal class SimpleSender : Sender {
    override fun send(client: Client, obj: Any) {
        client.sendUDP(obj)
    }
}

internal class LaggingSender(
    private val latency: Latency,
    private val impl: Sender
) : Sender {

    private val executors = Executors.newScheduledThreadPool(1)
    override fun send(client: Client, obj: Any) {
        executors.schedule(
            { impl.send(client, obj) },
            latency.random(),
            java.util.concurrent.TimeUnit.MILLISECONDS
        )
    }
}
