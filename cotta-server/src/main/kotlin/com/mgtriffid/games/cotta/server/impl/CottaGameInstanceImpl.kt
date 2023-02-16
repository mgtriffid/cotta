package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.TICK_LENGTH
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl
import com.mgtriffid.games.cotta.core.input.NonPlayersInput
import com.mgtriffid.games.cotta.core.input.PlayersInput
import com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.server.CottaGameInstance
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.IncomingInput
import com.mgtriffid.games.cotta.server.ServerSimulation
import com.mgtriffid.games.cotta.server.ServerToClientDataChannel
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class CottaGameInstanceImpl(
    val game: CottaGame,
    val network: CottaServerNetwork
): CottaGameInstance {
    private val historyLength = 8
    @Volatile var running = true
    private val tickProvider = TickProvider.getInstance()
    private val state = CottaStateImpl(historyLength, tickProvider)
    private val serverSimulation = ServerSimulation.getInstance(tickProvider, historyLength)
    private val clientsGhosts = ClientsGhosts()
    // should be generated by network? Yes, probably.
    private val serverToClientDataChannel = ServerToClientDataChannel.getInstance(
        tickProvider = tickProvider,
        clientsGhosts = clientsGhosts,
        network = network
    )

    override fun run() {
        network.initialize()
        initializeState()
        registerSystems()
        val loop = FixedRateLoopBody(
            tickLengthMs = TICK_LENGTH,
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
        val intents = network.drainEnterGameIntents()
        logger.debug { "intents.size == ${intents.size}" }
        intents.forEach {
            logger.debug { "Received an intent to enter the game" }
            val playerId = serverSimulation.enterGame(it.second)
            clientsGhosts.addGhost(playerId, it.first)
        }
        val input = fetchIncomingInput(network)
        serverSimulation.setInputForUpcomingTick(input)
    }

    private fun fetchIncomingInput(network: CottaServerNetwork): IncomingInput {
        return object: IncomingInput {
            override fun inputsForEntities(): Map<Int, Set<InputComponent<*>>> = emptyMap()
        }
    }

    private fun simulate() {
        serverSimulation.tick()
    }

    private fun dispatchDataToClients() {
        logger.info { "Preparing data to sent to clients" }
        /*
          TODO consider passing tick as a parameter here because it's confusing right now:
            tick goes through EVERYTHING but implicitly
         */
        val data = serverSimulation.getDataToBeSentToClients()
        send(data)
    }

    private fun send(data: DataForClients) {
        serverToClientDataChannel.send(data)
    }

    private fun getDataForClients(): DataForClients {
        TODO()
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
