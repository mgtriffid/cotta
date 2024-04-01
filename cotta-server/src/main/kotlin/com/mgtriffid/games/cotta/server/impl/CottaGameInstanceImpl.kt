package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.simulation.AuthoritativeSimulation
import com.mgtriffid.games.cotta.core.simulation.PlayersDiff
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.server.*
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class CottaGameInstanceImpl<IR: InputRecipe> @Inject constructor(
    private val game: CottaGame,
    private val componentRegistry: ComponentRegistry,
    private val network: CottaServerNetworkTransport,
    private val clientsGhosts: ClientsGhosts<IR>,
    @Named(SIMULATION) private val tickProvider: TickProvider,
    @Named("simulation") private val state: CottaState,
    private val serverToClientDataDispatcher: ServerToClientDataDispatcher,
    private val serverSimulation: AuthoritativeSimulation,
    private val serverSimulationInputProvider: ServerSimulationInputProvider
): CottaGameInstance {

    @Volatile
    var running = true

    private val playerIdGenerator = PlayerIdGenerator()

    override fun run() {
        registerComponents(game, componentRegistry)
        initializeState()
        registerSystems()
        logger.debug { "Tick length is ${game.config.tickLength}" }
        val loop = FixedRateLoopBody(
            tickLengthMs = game.config.tickLength,
            startsAt = System.currentTimeMillis()
        ) {
            tick()
        }
        loop.start()
    }

    private fun initializeState() {
        game.initializeStaticState(state.entities(tickProvider.tick))
        state.setBlank(state.entities(tickProvider.tick))
        game.initializeServerState(state.entities(tickProvider.tick))
    }

    private fun registerSystems() {
        game.serverSystems.forEach { serverSimulation.registerSystem(it as KClass<CottaSystem>) }
    }

    private fun tick() {
        serverSimulation.tick(fetchInput())
        dispatchDataToClients()
    }

    private fun fetchInput(): SimulationInput {
        serverSimulationInputProvider.fetch()
        val delta = serverSimulationInputProvider.getDelta()
        val intents = network.drainEnterGameIntents()
        val addedPlayers = intents.map { (connectionId, _) ->
            logger.debug { "Received an intent to enter the game from connection '${connectionId.id}'" }
            val playerId = playerIdGenerator.nextId()
            clientsGhosts.addGhost(playerId, connectionId)
            playerId
        }.toSet()
        return object : SimulationInput by delta.input {
            override fun playersDiff(): PlayersDiff {
                return PlayersDiff(addedPlayers)
            }
        }
    }

    private fun dispatchDataToClients() {
        logger.debug { "Preparing data to send to clients" }
        serverToClientDataDispatcher.dispatch()
    }

    private class PlayerIdGenerator {
        val counter = AtomicInteger(0)

        fun nextId(): PlayerId = PlayerId(counter.incrementAndGet())
    }
}
