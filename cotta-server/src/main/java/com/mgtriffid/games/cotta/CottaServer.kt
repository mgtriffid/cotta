package com.mgtriffid.games.cotta

import java.lang.Thread.sleep

class CottaServer {
    var nextTickAt = now()

    fun start() {
        while (true) {
            integrate()
            waitUntilNextTick()
        }
    }

    private fun integrate() {
        println("New tick")
    }

    private fun waitUntilNextTick() {
        nextTickAt += 20L
        (nextTickAt - now()).takeIf { it > 0 }?.let { sleep(it) }
    }

    private fun now() = System.currentTimeMillis()
}
