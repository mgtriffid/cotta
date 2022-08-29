package com.mgtriffid.games.cotta

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.network.CottaNetwork
import java.lang.Thread.sleep

class CottaServer(
    private val game: CottaGame,
    private val network: CottaNetwork
) {
    var nextTickAt = now()
    private val purgatory = ServerCodePurgatory(game, network)

    fun start() {
        purgatory.state = game.initialState()
        purgatory.initializeNetwork()
        while (true) {
            integrate()
            waitUntilNextTick()
        }
    }

    private fun integrate() {
        val playerInput = purgatory.getPlayerInputs()
        val nonPlayerInput = purgatory.getNonPlayerInput()
        purgatory.state = game.applyInput(purgatory.state, nonPlayerInput)
        println(purgatory.state)
    }

    private fun waitUntilNextTick() {
        nextTickAt += 20L
        (nextTickAt - now()).takeIf { it > 0 }?.let { sleep(it) }
    }

    private fun now() = System.currentTimeMillis()
}
