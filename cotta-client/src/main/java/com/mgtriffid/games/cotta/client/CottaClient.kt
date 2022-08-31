package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.network.CottaNetwork

class CottaClient(
    val game: CottaGame,
    val network: CottaNetwork
) {
    private val purgatory = ClientCodePurgatory(game, network)

    var nextTickAt = now()

    fun initialize() {
        purgatory.initializeNetwork()
    }

    // can be called many times!
    fun update() {
        purgatory.render()
        while (nextTickAt < now()) {
            purgatory.integrate()
            nextTickAt += 20L
        }
    }

    private fun now() = System.currentTimeMillis()
}
