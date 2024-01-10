package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistry
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.server.*
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class CottaGameInstanceImpl<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> @Inject constructor(
    private val game: CottaGame,
    private val componentsRegistry: ComponentsRegistry,
    private val network: CottaServerNetworkTransport,
    private val clientsGhosts: ClientsGhosts,
    private val tickProvider: TickProvider,
    @Named("simulation") private val state: CottaState,
    private val serverToClientDataDispatcher: ServerToClientDataDispatcher,
    private val entitiesCreatedOnClientsRegistry: EntitiesCreatedOnClientsRegistry,
    private val serverSimulation: ServerSimulation,
    private val serverSimulationInputProvider: ServerSimulationInputProvider
): CottaGameInstance {

    @Volatile
    var running = true

    override fun run() {
        registerComponents()
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

    // TODO probably this is wrong place
    private fun registerComponents() {
        game.componentClasses.forEach {
            componentsRegistry.registerComponentClass(it)
        }
        game.inputComponentClasses.forEach {
            componentsRegistry.registerInputComponentClass(it)
        }
        game.effectClasses.forEach {
            componentsRegistry.registerEffectClass(it)
        }
        serverSimulation.setMetaEntitiesInputComponents(game.metaEntitiesInputComponents)
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
        entitiesCreatedOnClientsRegistry.populate(delta.createdEntities)
        val intents = network.drainEnterGameIntents()
        intents.forEach { (connectionId, intent) ->
            registerPlayer(connectionId, intent)
        }
        return delta.input
    }

    private fun registerPlayer(connectionId: ConnectionId, intent: EnterGameIntent) {
        logger.trace { "Received an intent to enter the game from connection '${connectionId.id}'" }
        val playerId = serverSimulation.enterGame(intent)
        clientsGhosts.addGhost(playerId, connectionId)
    }

    private fun dispatchDataToClients() {
        logger.debug { "Preparing data to send to clients" }
        serverToClientDataDispatcher.dispatch()
    }
}
