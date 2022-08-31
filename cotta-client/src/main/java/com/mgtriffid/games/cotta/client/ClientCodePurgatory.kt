package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.CottaNetwork

class ClientCodePurgatory(
    private val game: CottaGame,
    private val network: CottaNetwork) {
    lateinit var clientNetwork: CottaClientNetwork

    fun initializeNetwork() {
        clientNetwork = network.createClientNetwork()
    }

    fun render() {
        snapInput()
        actuallyRender()
        println("Purgatory Rendering")
    }

    fun integrate() {
        println("Purgatory integrating")
    }

    private fun snapInput() {

    }

    private fun actuallyRender() {

    }
}
