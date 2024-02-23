package com.mgtriffid.games.cotta.network.kryonet.client

import com.esotericsoftware.kryonet.Client
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
    private val lag: Long,
    private val impl: Sender
) : Sender {

    private val executors = Executors.newScheduledThreadPool(1)
    override fun send(client: Client, obj: Any) {
        executors.schedule(
            { impl.send(client, obj) },
            lag,
            java.util.concurrent.TimeUnit.MILLISECONDS
        )
    }
}
