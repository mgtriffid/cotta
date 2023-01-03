package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.input.NonPlayersInput
import com.mgtriffid.games.cotta.core.input.PlayersInput
import com.mgtriffid.games.cotta.core.input.impl.GameInputImpl
import com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

interface CottaGameInstance {
    fun run()
}

class CottaGameInstanceImpl(
    val game: CottaGame,
    val state: CottaState
): CottaGameInstance {
    @Volatile var running = true

    override fun run() {
        initializeState()
        val loop = FixedRateLoopBody(
            tickLengthMs = 20L,
            startsAt = System.currentTimeMillis()
        ) {
            tick()
        }
        loop.start()
    }

    private fun initializeState() {
        game.initializeServerState(state)
    }

    private fun tick() {
        logger.info { "Tick is happening" }
        fetchInput()
        simulate()
        // simulation after fetching input
        // effects after simulation
        // no actually effects within simulation, they are effects FFS
        // when do we dispatch effects? On this tick or on the next tick?
        // If it's, say, hitscan. On current tick Orbb shoots Bones with a railgun dealing 85.
        // Now imagine it's not deterministically 85, it's some random thing. This "random" part is likely a non-player
        // input thing. For example we may have an algorithm for pseudorandom that acts on entities and some seed.
    }

    private fun fetchInput() {
        val playersInput = fetchFromNetwork()
        val nonPlayersInput = calculateNonPlayerInput()
        val gameInput = GameInputImpl(playersInput, nonPlayersInput)
        stuffInputIntoEntities()
    }

    private fun simulate() {

    }

    private fun fetchFromNetwork(): PlayersInput {
        logger.debug { "Fetching input from network" }
        return object: PlayersInput {}
    }

    private fun stuffInputIntoEntities() {

    }

    private fun calculateNonPlayerInput(): NonPlayersInput {
        logger.debug { "Calculating non-player input" }
        return object : NonPlayersInput {}
    }
}
