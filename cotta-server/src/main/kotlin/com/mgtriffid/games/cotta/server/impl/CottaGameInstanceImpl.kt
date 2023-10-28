package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.loop.impl.FixedRateLoopBody
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.server.CottaGameInstance
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.ServerSimulation
import com.mgtriffid.games.cotta.server.ServerSimulationInputProvider
import com.mgtriffid.games.cotta.server.ServerToClientDataChannel
import jakarta.inject.Inject
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class CottaGameInstanceImpl<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> @Inject constructor(
    val game: CottaGame,
    val engine: CottaEngine<SR, DR, IR>,
    val network: CottaServerNetwork,
    val clientsGhosts: ClientsGhosts,
    val tickProvider: TickProvider,
    val state: CottaState,
    private val serverToClientDataChannel: ServerToClientDataChannel,
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
            engine.getComponentsRegistry().registerComponentClass(it)
        }
        game.inputComponentClasses.forEach {
            engine.getComponentsRegistry().registerInputComponentClass(it)
        }
        serverSimulation.setMetaEntitiesInputComponents(game.metaEntitiesInputComponents)
    }

    private fun initializeState() {
        game.initializeServerState(state)
    }

    private fun registerSystems() {
        game.serverSystems.forEach { serverSimulation.registerSystem(it as KClass<CottaSystem>) }
    }

    private fun tick() {
        fetchInput()
        serverSimulation.tick()
        dispatchDataToClients()
    }

    private fun fetchInput() {
        val intents = network.drainEnterGameIntents()
        intents.forEach { (connectionId, intent) ->
            registerPlayer(connectionId, intent)
        }
        serverSimulationInputProvider.prepare()
    }

    private fun registerPlayer(connectionId: ConnectionId, intent: EnterGameIntent) {
        logger.trace { "Received an intent to enter the game from connection '${connectionId.id}'" }
        val playerId = serverSimulation.enterGame(intent)
        clientsGhosts.addGhost(playerId, connectionId)
    }

    private fun dispatchDataToClients() {
        logger.debug { "Preparing data to send to clients" }
        val data = serverSimulation.getDataToBeSentToClients()
        send(data)
    }

    private fun send(data: DataForClients) {
        serverToClientDataChannel.send(data)
    }
}
