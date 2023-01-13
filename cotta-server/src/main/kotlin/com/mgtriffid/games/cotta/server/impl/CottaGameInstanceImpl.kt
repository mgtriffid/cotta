package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.input.NonPlayersInput
import com.mgtriffid.games.cotta.core.input.PlayersInput
import com.mgtriffid.games.cotta.core.input.impl.GameInputImpl
import com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.server.CottaGameInstance
import com.mgtriffid.games.cotta.server.ServerSimulation
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class CottaGameInstanceImpl(
    val game: CottaGame,
    val state: CottaState
): CottaGameInstance {
    @Volatile var running = true

    private val serverSimulation = ServerSimulation.getInstance()

    override fun run() {
        initializeState()
        registerSystems()
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
        serverSimulation.setState(state)
    }

    private fun registerSystems() {
        game.serverSystems.forEach { serverSimulation.registerSystem(it as KClass<CottaSystem>) }
    }

    private fun tick() {
        logger.info { "Tick is happening" }
        fetchInput()
        simulate()
        // simulation after fetching input
        // effects after simulation
        // no actually effects within simulation, they are effects FFS
        dispatchDataToClients()
    }

    private fun fetchInput() {

    }

    private fun simulate() {
        serverSimulation.tick()
    }

    private fun dispatchDataToClients() {
        serverSimulation.getDataToBeSentToClients()
    }

    private fun fetchFromNetwork(): PlayersInput {
        logger.debug { "Fetching input from network" }
        return object: PlayersInput {}
    }

    private fun calculateNonPlayerInput(): NonPlayersInput {
        logger.debug { "Calculating non-player input" }
        return object : NonPlayersInput {}
    }
}
