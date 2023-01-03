package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.input.NonPlayersInput
import com.mgtriffid.games.cotta.core.input.PlayersInput
import com.mgtriffid.games.cotta.core.input.impl.GameInputImpl
import com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody
import com.mgtriffid.games.cotta.server.CottaGameInstance
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

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
