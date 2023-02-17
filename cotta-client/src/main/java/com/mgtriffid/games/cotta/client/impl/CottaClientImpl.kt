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

    override fun tick() {
        logger.info { "Running ${CottaClientImpl::class.simpleName}" }
        updateNetwork() // means "fetch all the incoming shit, maybe send EtG if not sent yet".
        // should network.update() include connection? Yes it should.
        // now there are two major approaches. One is to do this "states" BS and track how it changes,
        // and behave differently. Another is to go full FP mode. Which is probably a bit less performant
        // but super solid.
        // Possible states are
        // - starting
        // - connecting
        // no actually no need to tie them all together. It's a good concept to have simulation inside, and
        // it's a good concept to have markers for "state quality" like we did in Kata. So simulation has states
        // inside.
        // clientSimulation 1-* State (per tick)

        // We have sent entergameintent, we chill until it arrives. What does it mean "we chill"? It means we
        // don't simulate and every tick we poll for a state snapshot.
        // so in fact logic is quite similar to what we had in Kata: depending on what we have rn we either
        // simulate regularly or chill or try to guess or try to reconcile to fix after some mistakes when
        // our guess was not correct.
    }

    // very similar to server's `fetchInput` if we think about it
    private fun updateNetwork() {
        if (connected) {
            fetchData()
        } else {
            connect()
        }
    }

    private fun fetchData() {
        // take data and put it into buffers
        // data can be of two kinds (so far): state packets, delta packets.
        // we stuff it in and then make a decision what to do.
    }

    private fun connect() {
        network.initialize()
        network.sendEnterGameIntent()
        connected = true
    }
}
