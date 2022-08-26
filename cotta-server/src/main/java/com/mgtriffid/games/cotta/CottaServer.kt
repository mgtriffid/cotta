package com.mgtriffid.games.cotta

import com.mgtriffid.games.cotta.core.CottaGame
import java.lang.Thread.sleep

class CottaServer(
    private val game: CottaGame
) {
    var nextTickAt = now()

    fun start() {
        while (true) {
            integrate()
            waitUntilNextTick()
        }
    }

    private fun integrate() {
        game.update()
    }

    private fun waitUntilNextTick() {
        nextTickAt += 20L
        (nextTickAt - now()).takeIf { it > 0 }?.let { sleep(it) }
    }

    private fun now() = System.currentTimeMillis()
}
