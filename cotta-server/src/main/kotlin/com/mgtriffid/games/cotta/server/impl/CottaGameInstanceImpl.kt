package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.CreatingStaticEntities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.simulation.Simulation
import com.mgtriffid.games.cotta.core.simulation.PlayersDiff
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
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
    private val serverSimulation: Simulation,
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
        game.initializeStaticState(CreatingStaticEntities(state.entities(tickProvider.tick)))
        state.setBlank(state.entities(tickProvider.tick))
        game.initializeServerState(state.entities(tickProvider.tick))
    }

    private fun registerSystems() {
        game.systems.forEach(serverSimulation::registerSystem)
    }

    private fun tick() {
        serverSimulation.tick(fetchInput())
        dispatchDataToClients()
    }

    private fun fetchInput(): SimulationInput {
        serverSimulationInputProvider.fetch()
        val delta = serverSimulationInputProvider.getDelta()
        val etgIntents = network.drainEnterGameIntents()
        val disconnects = network.drainDisconnects()
        val addedPlayers = etgIntents.mapNotNull { (connectionId, _) ->
            logger.debug { "Received an intent to enter the game from connection '${connectionId.id}'" }
            if (clientsGhosts.playerByConnection[connectionId] != null) {
                return@mapNotNull null
            }
            val playerId = playerIdGenerator.nextId()
            clientsGhosts.addGhost(playerId, connectionId)
            playerId
        }.toSet()
        val removedPlayers = disconnects.mapNotNull { connectionId ->
            logger.debug { "Received a disconnect from connection '${connectionId.id}'" }
            val playerId = clientsGhosts.playerByConnection[connectionId]
            if (playerId != null) {
                clientsGhosts.removeGhost(playerId)
                return@mapNotNull playerId
            }
            null
        }.toSet()
        return object : SimulationInput by delta.input {
            override fun playersDiff(): PlayersDiff {
                return PlayersDiff(addedPlayers, removedPlayers)
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
