package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CottaClientImpl(
    val game: CottaGame,
    val network: CottaClientNetwork
) : CottaClient {
    var connected = false
    private val incomingDataBuffer = IncomingDataBuffer()
    private var state: ClientState = ClientState.Initial

    override fun tick() {
        logger.info { "Running ${CottaClientImpl::class.simpleName}" }
        when (state) {
            ClientState.Initial -> {
                connect()
                state = ClientState.AwaitingGameState(since = System.currentTimeMillis())
            }
            is ClientState.AwaitingGameState -> {
                // if state came then all good, we are running, integrate
                // else if it's too long then we disconnect
                // else we do nothing and wait further
            }
            ClientState.Disconnected -> {
                // we call disconnectedlistener? So that player could set screen to something, idk. We may employ
                // different strategies what to do, depending on needs. Maybe reconnect, but normally just disc.
            }
            is ClientState.Lagging -> {
                // either wait or run or disconnect
            }
            is ClientState.Running -> {
                // if we can integrate we integrate
            }
        }
    }

    private fun fetchData() {

        // take data from queues and put it into buffers, deserialize, etc.
        // data can be of two kinds (so far): state packets, delta packets.
        // it also can be absent
        // we stuff it in and then make a decision what to do.
    }

    private fun connect() {
        network.initialize()
        network.sendEnterGameIntent()
    }

    sealed class ClientState {
        object Initial: ClientState()
        class AwaitingGameState(since: Long) : ClientState()
        object Disconnected: ClientState()
        class Lagging(sinceTick: Long): ClientState() // not sure
        class Running(currentTick: Long): ClientState() // not sure again
    }
}
