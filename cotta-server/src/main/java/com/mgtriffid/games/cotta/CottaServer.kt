package com.mgtriffid.games.cotta

import com.mgtriffid.games.cotta.core.CottaGame
import java.lang.Thread.sleep

class CottaServer(
    private val game: CottaGame
) {
    var nextTickAt = now()
    private val purgatory = ServerCodePurgatory(game)

    fun start() {
        purgatory.state = game.initialState()
        while (true) {
            integrate()
            waitUntilNextTick()
        }
    }

    private fun integrate() {
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
