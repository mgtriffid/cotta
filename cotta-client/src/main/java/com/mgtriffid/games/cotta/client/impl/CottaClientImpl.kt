package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.utils.now
import mu.KotlinLogging
import kotlin.math.log

const val STATE_WAITING_THRESHOLD = 5000L

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
        state.let {
            when (it) {
                ClientState.Initial -> {
                    connect()
                    state = ClientState.AwaitingGameState(since = System.currentTimeMillis())
                }

                is ClientState.AwaitingGameState -> {
                    fetchData()
                    if (stateAvailable()) {
                        setStateFromAuthoritative()
                        state = ClientState.Running(getCurrentTick())
                    } else {
                        if (now() - it.since > STATE_WAITING_THRESHOLD) {
                            state = ClientState.Disconnected
                        }
                    }
                }

                ClientState.Disconnected -> {
                    // TODO
                }

                is ClientState.Running -> {
                    fetchData()
                    integrate()
                    state = ClientState.Running(it.currentTick + 1)
                }
            }
        }
    }

    private fun setStateFromAuthoritative() {
        logger.debug { "Setting state from authoritative" }
        // TODO("Not yet implemented")
    }

    private fun getCurrentTick(): Long {
//        TODO("Not yet implemented")
        return 1L
    }

    private fun integrate() {
        logger.debug { "Integrating" }
//        TODO()
    }

    private fun fetchData() {
        val data = network.drainIncomingData()
        data.forEach ( incomingDataBuffer::store )
        // take data from queues and put it into buffers, deserialize, etc.
        // data can be of two kinds (so far): state packets, delta packets.
        // it also can be absent
        // we stuff it in and then make a decision what to do.
    }

    private fun stateAvailable(): Boolean {
        return true
    }

    private fun connect() {
        network.initialize()
        network.sendEnterGameIntent()
    }

    sealed class ClientState {
        object Initial : ClientState()
        class AwaitingGameState(val since: Long) : ClientState()
        object Disconnected : ClientState()
        class Running(val currentTick: Long) : ClientState() // not sure again
    }
}
